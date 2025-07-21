package com.tiger.config;

import com.tiger.dto.LinkedInUserInfoDTO;
import com.tiger.dto.UserDTO;
import com.tiger.entity.User;
import com.tiger.repository.UserRepository;
import com.tiger.service.LinkedInAuthenticationToken;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.LocalDateTime;

@Configuration
public class LinkedInTokenValidationFilter extends OncePerRequestFilter {
	private UserRepository userRepo ;
	public LinkedInTokenValidationFilter(UserRepository userRepo) {
		this.userRepo = userRepo ;
	}



    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {


        System.out.println("Filter triggered for URI: " + request.getRequestURI());
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid Authorization header");
            return;
        }

        String accessToken = null ;
        String code = authHeader.substring(7); 

        try {
        	 if(userRepo.findByCode(code).isEmpty())
             	throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED) ;
             User authUser = userRepo.findByCode(code).get();
              
            accessToken = authUser.getAccessToken() ;
            if(LocalDateTime.now().isAfter(authUser.getTokenExpiry()))
            	throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED) ;
            
			/*
			 * WebClient webClient = WebClient.builder()
			 * .baseUrl("https://api.linkedin.com")
			 * .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
			 * 
			 * .build();
			 * 
			 * LinkedInUserInfoDTO userInfo = webClient.get() .uri("/v2/userinfo")
			 * .retrieve() .bodyToMono(LinkedInUserInfoDTO.class) .block();
			 * 
			 * String email = userInfo.getEmail();
			 */
            String email = authUser.getGmail() ;
            UserDTO user = new UserDTO();
            user.setGmail(email);
            LinkedInAuthenticationToken auth = new LinkedInAuthenticationToken(user);
            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid LinkedIn token/expired: " + ex.getMessage());
            return ;
        }
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        return path.equals("/api/tiger/user/linkedin/login") ||
               path.equals("/api/tiger/user/linkedin/callback")||
               path.startsWith("/api/tiger/leetcode/profile/");
    }
}
