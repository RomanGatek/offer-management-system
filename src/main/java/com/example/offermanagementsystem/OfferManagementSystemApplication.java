package com.example.offermanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OfferManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(OfferManagementSystemApplication.class, args);
	}

}