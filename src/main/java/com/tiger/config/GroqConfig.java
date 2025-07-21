package com.tiger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
@Data
@ConfigurationProperties(prefix = "groq")
public class GroqConfig {
	private String dailyRoastPrompt ;
	private String profileRoastPrompt ;
	private String model ;
	private Integer retryLatency ;
	private String notifyMorning ;
	private String notifyWork ;
}
