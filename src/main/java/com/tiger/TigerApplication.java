package com.tiger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.tiger.config.GroqConfig;
import com.tiger.config.UrlConfig;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
@SpringBootApplication
@EnableConfigurationProperties({UrlConfig.class , GroqConfig.class })
@EnableScheduling
public class TigerApplication {
	public static void main(String[] args) {
		
		/*
		 * Dotenv dotenv = Dotenv.load();
		 * 
		 * System.setProperty("api.leetcode-url", dotenv.get("API_LEETCODE_URL"));
		 * System.setProperty("api.github-token", dotenv.get("API_GITHUB_TOKEN"));
		 * System.setProperty("api.groq-token", dotenv.get("API_GROQ_TOKEN"));
		 * System.setProperty("api.linkedin-client-id",
		 * dotenv.get("API_LINKEDIN_CLIENT_ID"));
		 * System.setProperty("api.linkedin-client-secret",
		 * dotenv.get("API_LINKEDIN_CLIENT_SECRET"));
		 * System.setProperty("api.linkedin-callback",
		 * dotenv.get("API_LINKEDIN_CALLBACK")); System.setProperty("api.redirect-uri",
		 * dotenv.get("LINKEDIN_REDIRECT_URI"));
		 * System.setProperty("api.firebase-access-token",
		 * dotenv.get("API_FIREBASE_ACCESS_TOKEN"));
		 * System.setProperty("api.firebase-refresh-token",
		 * dotenv.get("API_FIREBASE_REFRESH_TOKEN"));
		 * System.setProperty("api.google-clientid", dotenv.get("API_GOOGLE_CLIENTID"));
		 * System.setProperty("api.google-client-secret",
		 * dotenv.get("API_GOOGLE_CLIENT_SECRET"));
		 * System.setProperty("spring.datasource.url",
		 * dotenv.get("SPRING_DATASOURCE_URL"));
		 * System.setProperty("linkedin.client-id", dotenv.get("LINKEDIN_CLIENT_ID"));
		 * System.setProperty("linkedin.client-secret",
		 * dotenv.get("LINKEDIN_CLIENT_SECRET"));
		 * System.setProperty("linkedin.redirect-uri",
		 * dotenv.get("LINKEDIN_REDIRECT_URI"));
		 * 
		 */
		SpringApplication.run(TigerApplication.class, args);
	}
}
