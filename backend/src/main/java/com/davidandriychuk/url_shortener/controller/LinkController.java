package com.davidandriychuk.url_shortener.controller;

import com.davidandriychuk.url_shortener.dto.AuthResponseDTO;
import com.davidandriychuk.url_shortener.dto.JwtResponse;
import com.davidandriychuk.url_shortener.dto.LoginRequest;
import com.davidandriychuk.url_shortener.security.JWTGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;import com.davidandriychuk.url_shortener.entity.Link;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import com.davidandriychuk.url_shortener.entity.User;
import com.davidandriychuk.url_shortener.repository.UserRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import com.davidandriychuk.url_shortener.repository.LinkRepository;
import com.davidandriychuk.url_shortener.service.LinkService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.AmqpException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import com.davidandriychuk.url_shortener.dto.ClickEventMessage;
import com.davidandriychuk.url_shortener.config.RabbitConfig;

import static com.davidandriychuk.url_shortener.entity.Link.decodeShortCode;
import static com.davidandriychuk.url_shortener.entity.Link.generateShortCode;

@RestController
class LinkController{

    private final LinkRepository repository;
    private final UserRepository userRepository;
    private final LinkService linkService;
    private final RabbitTemplate rabbitTemplate;


    LinkController(LinkRepository repository, UserRepository userRepository, LinkService linkService, RabbitTemplate rabbitTemplate){
        this.repository = repository;
        this.userRepository = userRepository;
        this.linkService = linkService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @GetMapping("/links")
    List<Link> all() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + email));

        return repository.findByUserId(user.getId());
    }

    @PostMapping("/links")
    Link newLink(@RequestBody Map<String, String> request) {
        String originalUrl = request.get("url");

        Authentication authenication = SecurityContextHolder.getContext().getAuthentication();
        String email = authenication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + email));

        LocalDateTime created = LocalDateTime.now();
        LocalDateTime expires = created.plusHours(72);
        Link link = new Link(originalUrl, user.getId(),created, expires);
        link = repository.save(link);

        String shortCode = generateShortCode(link.getId());
        link.setShortCode(shortCode);
        link = repository.save(link);

        return link;
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