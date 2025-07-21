package com.tiger.service;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import com.tiger.dto.UserDTO;

public class LinkedInAuthenticationToken extends AbstractAuthenticationToken
{

    private final UserDTO principal;

    public LinkedInAuthenticationToken(UserDTO
    		principal) {
        super(null);
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public Object getCredentials() {
        return null;
    }
}
