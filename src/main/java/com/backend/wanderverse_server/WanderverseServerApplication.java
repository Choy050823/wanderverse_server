package com.backend.wanderverse_server;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class WanderverseServerApplication {

	private static final Logger logger = LoggerFactory.getLogger(WanderverseServerApplication.class);

	// Inject the value of spring.datasource.url from application.properties
	@Value("${spring.datasource.url}")
	private String datasourceUrl;

	public static void main(String[] args) {
		SpringApplication.run(WanderverseServerApplication.class, args);
	}

	@PostConstruct
	public void logDatasourceUrl() {
		logger.info(datasourceUrl);
	}

}
