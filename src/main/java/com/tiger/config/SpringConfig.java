package com.tiger.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class SpringConfig {
	private UrlConfig url ;
	public SpringConfig(UrlConfig url) {
		this.url = url ;
	}
	@Bean(name = "github")
	public WebClient gitHubClient() {
		return WebClient.builder()
				.baseUrl(url.getGithubUrl()) 
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + url.getGithubToken()) 
				.build() ;
	}
	@Bean(name = "leetcode")
	public WebClient leetcodeClient() {
		return WebClient.builder()
				.baseUrl(url.getLeetcodeUrl())
				.build() ;
	}
	@Bean(name = "linkedin")
	public WebClient linkedIn() {
		return WebClient.builder()
				.baseUrl(url.getLinkedinUrl())
				.build() ;
	}
	@Bean(name = "firebase")
	public WebClient firebase() {
		return WebClient.builder()
				.baseUrl(url.getFirebaseAccessUrl())
				.build() ;
	}
	@Bean(name="groq")
	public WebClient groqClient() {
		return WebClient.builder()
				.baseUrl(url.getGroqUrl())
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + url.getGroqToken() ) 
				.build() ; 
	}
	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper()  .registerModule(new JavaTimeModule())
			    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}
	@Bean 
	public ModelMapper modelMapper() {
		return new ModelMapper() ;
	}
}
