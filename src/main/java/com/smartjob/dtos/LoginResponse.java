package com.smartjob.dtos;

import com.smartjob.persistence.entities.User;


public class LoginResponse {
    private User user;
    private AuthenticationResponse authenticationResponse;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public AuthenticationResponse getAuthenticationResponse() {
        return authenticationResponse;
    }

    public void setAuthenticationResponse(AuthenticationResponse authenticationResponse) {
        this.authenticationResponse = authenticationResponse;
    }
}
