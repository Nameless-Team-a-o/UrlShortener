package com.nameless.urlshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UrlshortenerApplication {

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "server1");
		SpringApplication.run(UrlshortenerApplication.class, args);
	}

}
