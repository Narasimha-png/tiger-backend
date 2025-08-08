package com.tiger.service.implementation;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.tiger.config.UrlConfig;

import reactor.core.publisher.Mono;

@Service
public class FirebaseNotificationService {

	private UrlConfig urlConfig ;
	private WebClient firebaseClient ;
	private String accessToken ;
	private final WebClient webClient = WebClient.create("https://oauth2.googleapis.com");
	private static final Logger LOGGER = LogManager.getLogger(FirebaseNotificationService.class) ;
	
	public FirebaseNotificationService(@Qualifier("firebase") WebClient firebaseClient, UrlConfig urlConfig) {
		this.firebaseClient = firebaseClient ;
		this.urlConfig = urlConfig ;
		this.accessToken = urlConfig.getFirebaseAccessToken() ;
	}
	
	
	
	public String refreshAccessToken() {
	    try {
	        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
	        formData.add("grant_type", "refresh_token");
	        formData.add("client_id", urlConfig.getGoogleClientid());
	        formData.add("client_secret",urlConfig.getGoogleClientSecret());
	        formData.add("refresh_token", urlConfig.getFirebaseRefreshToken());

	        Map<String, Object> result = webClient.post()
	                .uri("/token")
	                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
	                .body(BodyInserters.fromFormData(formData))
	                .retrieve()
	                .bodyToMono(Map.class)
	                .block();

	        return result != null ? (String) result.get("access_token") : null;

	    } catch (Exception e) {
	        LOGGER.error("Error refreshing token " , e );
	        return null;
	    }
	}
		public void sendNotification(String accessToken, String targetToken, String title, String body) {
	       
	        if (accessToken == null) {
	            System.err.println("Access token is null, cannot send notification.");
	            return;
	        }

	        Map<String, Object> message = new HashMap<>();
	        Map<String, String> notification = new HashMap<>();
	        notification.put("title", title);
	        notification.put("body", body);

	        Map<String, Object> innerMessage = new HashMap<>();
	        innerMessage.put("token", targetToken);
	        innerMessage.put("notification", notification);

	        message.put("message", innerMessage);

	        try {
	        firebaseClient.post()
	                .uri("/v1/projects/" + urlConfig.getFirebaseProjectid() + "/messages:send")
	                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
	                .contentType(MediaType.APPLICATION_JSON)
	                .bodyValue(message)
	                .retrieve()
	                .bodyToMono(String.class)
	                .doOnError(error -> LOGGER.error("Error sending notification: " + error.getMessage()))
	                .block();
	        }catch(Exception e) {}
	    }
	  

	  
	    private static class TokenResponse {
	        private String access_token;

	        public String getAccess_token() {
	            return access_token;
	        }

	        public void setAccess_token(String access_token) {
	            this.access_token = access_token;
	        }
	    }
}
