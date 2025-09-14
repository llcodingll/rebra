package com.rebra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RebraApplication {

	public static void main(String[] args) {
		SpringApplication.run(RebraApplication.class, args);
	}

}
