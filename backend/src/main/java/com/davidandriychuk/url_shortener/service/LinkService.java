package com.davidandriychuk.url_shortener.service;

import com.davidandriychuk.url_shortener.entity.Link;
import com.davidandriychuk.url_shortener.repository.LinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import java.util.NoSuchElementException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;


@Service
public class LinkService{
    private final LinkRepository repository;

    public LinkService(LinkRepository repository){ this.repository = repository;}

    @Cacheable(value="links", key="#shortCode")
    public String getOriginalUrl(String shortCode){
        long id = Link.decodeShortCode(shortCode);
        Link link = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"No link for code: " + shortCode));
        if(link.getExpiresAt().isBefore(LocalDateTime.now())){
            throw new ResponseStatusException(HttpStatus.GONE, "Link Expired");
        }

        return link.getOriginalUrl();
    }
}
