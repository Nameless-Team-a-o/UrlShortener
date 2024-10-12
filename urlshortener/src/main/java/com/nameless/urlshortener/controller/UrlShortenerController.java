package com.nameless.urlshortener.controller;

import com.nameless.urlshortener.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UrlShortenerController {


    private final UrlShortenerService urlShortenerService;

    @PostMapping(value = "/shorten" , produces = "application/json")
    public ResponseEntity<String> shortenUrl(@RequestBody Map<String, String> body) {
        String originalUrl = body.get("url");

        // Validate URL format
        if (originalUrl == null || !isValidUrl(originalUrl)) {
            return ResponseEntity.badRequest().body("{\"error\":\"Invalid URL provided.\"}");
        }

        // Attempt to shorten the URL
        String shortUrl = urlShortenerService.shortenUrl(originalUrl);
        return ResponseEntity.ok("{\"shortUrl\":\"" + shortUrl + "\"}");
    }

    @GetMapping(value = "/{shortUrl}", produces = "application/json")
    public ResponseEntity<String> redirect(@PathVariable String shortUrl) {
        // Attempt to get the original URL
        String originalUrl = urlShortenerService.getOriginalUrl(shortUrl);

        if (originalUrl != null) {
            return ResponseEntity.ok("{\"originalUrl\":\"" + originalUrl + "\"}");
        } else {
            return ResponseEntity.status(HttpServletResponse.SC_NOT_FOUND)
                    .body("{\"error\":\"Short URL not found.\"}");
        }
    }

    private boolean isValidUrl(String url) {
        // Simple regex for URL validation
        String regex = "^(http://|https://|ftp://|ftps://)?([a-z0-9]+[.-]?)+(:[0-9]{1,5})?(/.*)?$";
        return url.matches(regex);
    }
}
