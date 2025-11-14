package com.barlog.loyaltyapi;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableScheduling

public class LoyaltyApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoyaltyApiApplication.class, args);
    }

}
