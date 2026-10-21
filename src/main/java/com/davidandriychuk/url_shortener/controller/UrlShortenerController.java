package com.davidandriychuk.url_shortener.controller;

//import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UrlShortenerController {

    private final
/*
    @GetMapping("/UrlShortener")
    public UrlShortener UrlShortener(@RequestParam(defaultValue = "World") String name) {
        return new UrlShortener(counter.incrementAndGet(), template.formatted(name));
    }

    @GetMapping("/Links")
    public UrlShortener

 */
}
/*
POST /links — takes a URL in the request body, generates a short code (this is where step 2's base62 algorithm goes), saves a Link, returns the short code
GET /{code} — looks up the Link by short code, and redirects to originalUrl (look up RedirectView or HttpServletResponse.sendRedirect() — either works)
 */