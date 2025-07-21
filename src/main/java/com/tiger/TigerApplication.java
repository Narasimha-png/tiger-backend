package com.tiger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.tiger.config.GroqConfig;
import com.tiger.config.UrlConfig;

@SpringBootApplication
@EnableConfigurationProperties({UrlConfig.class , GroqConfig.class })
@EnableScheduling
public class TigerApplication {
	public static void main(String[] args) {
		SpringApplication.run(TigerApplication.class, args);
	}
}
