package com.davidandriychuk.url_shortener.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.davidandriychuk.url_shortener.entity.Link;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;
import com.davidandriychuk.url_shortener.entity.User;
import com.davidandriychuk.url_shortener.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.davidandriychuk.url_shortener.repository.LinkRepository;
import com.davidandriychuk.url_shortener.service.LinkService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.AmqpException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import jakarta.servlet.http.HttpServletRequest;
import com.davidandriychuk.url_shortener.dto.ClickEventMessage;
import com.davidandriychuk.url_shortener.config.RabbitConfig;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import static com.davidandriychuk.url_shortener.entity.Link.generateShortCode;

@RestController
class LinkController{

    private final LinkRepository repository;
    private final UserRepository userRepository;
    private final LinkService linkService;
    private final RabbitTemplate rabbitTemplate;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket bucketFor(String email) {
        return buckets.computeIfAbsent(email, k -> Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1))))
                .build());
    }

    LinkController(LinkRepository repository, UserRepository userRepository, LinkService linkService, RabbitTemplate rabbitTemplate){
        this.repository = repository;
        this.userRepository = userRepository;
        this.linkService = linkService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @GetMapping("/api/links")
    ResponseEntity<?> all() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new NoSuchElementException("User not found: " + email));

            return ResponseEntity.ok(repository.findByUserId(user.getId()));
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR IN GET /api/links: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/api/links")
    ResponseEntity<?> newLink(@RequestBody Map<String, String> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        if (!bucketFor(email).tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Rate limit exceeded. Try again later."));
        }

        String originalUrl = request.get("url");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + email));

        LocalDateTime created = LocalDateTime.now();
        LocalDateTime expires = created.plusHours(72);
        Link link = new Link(originalUrl, user.getId(), created, expires);
        link = repository.save(link);

        link.setShortCode(generateShortCode(link.getId()));
        link = repository.save(link);

        return ResponseEntity.ok(link);
    }

    @GetMapping("/s/{shortCode}")
    RedirectView redirect(@PathVariable String shortCode, HttpServletRequest request) {
        ClickEventMessage message = new ClickEventMessage(
                shortCode,
                LocalDateTime.now().toString(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                request.getHeader("Referer")
        );
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(linkService.getOriginalUrl(shortCode));
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, message);
        } catch (AmqpException e) {
            //log and move on
        }
        return redirectView;
    }

}