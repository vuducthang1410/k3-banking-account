package com.example.smsHost;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.smsHost")
public class SmsHostApplication {
	public static void main(String[] args) {
		SpringApplication.run(SmsHostApplication.class, args);
	}

}
