package com.tiger.service;


import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.tiger.dto.UserDTO;

@Component
public class AuthService {
	public String getEmail() {
		var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDTO user) {
            return user.getGmail();
        }
        return null; 
	}
}
