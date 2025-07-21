package com.tiger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
	private final LinkedInTokenValidationFilter linkedInTokenValidationFilter;

    public SecurityConfig(LinkedInTokenValidationFilter filter) {
        this.linkedInTokenValidationFilter = filter;
    }
	@Bean
	 public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults()) 
        .csrf(csrf -> csrf.disable())
        
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .addFilterBefore(linkedInTokenValidationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
	
}
