package com.system.core_banking_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class CoreBankingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoreBankingServiceApplication.class, args);
	}

}
