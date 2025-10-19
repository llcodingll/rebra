package com.rebra.rebalance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RebalanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RebalanceApplication.class, args);
	}

}
