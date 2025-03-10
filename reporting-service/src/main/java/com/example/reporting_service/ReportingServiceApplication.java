package com.example.reporting_service;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class ReportingServiceApplication {

	public static void main(String[] args) {

		SpringApplication.run(ReportingServiceApplication.class, args);
	}

}
