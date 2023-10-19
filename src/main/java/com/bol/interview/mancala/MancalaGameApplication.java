package com.bol.interview.mancala;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableAutoConfiguration
@EnableMongoRepositories
public class MancalaGameApplication {
    public static void main(String[] args) {
        SpringApplication.run(MancalaGameApplication.class, args);
    }
}