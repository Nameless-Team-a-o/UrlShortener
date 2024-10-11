package com.nameless.urlshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App2Application {
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "server2");
        SpringApplication.run(App2Application.class, args);
    }
}
