package com.tiger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


import lombok.Data;

@Data
@ConfigurationProperties(prefix = "api")
public class UrlConfig {
	private String githubUrl ;
	private String leetcodeUrl ;
	private String githubToken ; 
	private String groqUrl ;
	private String groqToken ;
	private String linkedinClientId ;
	private String linkedinClientSecret ;
	private String linkedinCallback ;
	private String redirectUrl;
	private Integer groqRetryCount ;
	private String linkedinUrl ;
	private String firebaseAccessUrl ;
	private String firebaseAccessToken ;
	private String firebaseRefreshToken ;
	private String firebaseProjectid ;
	private String googleClientid ;
	private String googleClientSecret ;
	private String myUrl ;
	private String myDomain ;
}
