package com.barlog.loyaltyapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling

public class LoyaltyApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoyaltyApiApplication.class, args);
	}

}
