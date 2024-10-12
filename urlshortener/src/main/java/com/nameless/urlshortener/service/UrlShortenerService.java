package com.nameless.urlshortener.service;

import com.nameless.urlshortener.model.UrlMapping;
import com.nameless.urlshortener.repository.UrlMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UrlShortenerService {
    private final UrlMappingRepository urlMappingRepository;


    private final  SnowflakeGenerator idGenerator; // Snowflake ID generator

    public String shortenUrl(String originalUrl) {
        long id = idGenerator.generateNextId(); // Generate Snowflake ID
        String shortUrl = convertToBase62(id); // Convert to Base62

        UrlMapping mapping = new UrlMapping();
        mapping.setId(id);
        mapping.setOriginalUrl(originalUrl);
        mapping.setShortUrl(shortUrl);
        urlMappingRepository.save(mapping); // Save mapping to the database

        return shortUrl;
    }

    public String getOriginalUrl(String shortUrl) {
        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        System.out.println(urlMapping);
        return urlMapping != null ? urlMapping.getOriginalUrl() : null;
    }

    private String convertToBase62(long id) {
        // Base62 conversion logic
        StringBuilder sb = new StringBuilder();
        String characters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        while (id > 0) {
            sb.append(characters.charAt((int) (id % 62)));
            id /= 62;
        }
        return sb.reverse().toString();
    }
}
