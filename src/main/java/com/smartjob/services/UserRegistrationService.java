package com.smartjob.services;

import com.smartjob.dtos.AuthenticationRequest;
import com.smartjob.dtos.AuthenticationResponse;
import com.smartjob.persistence.entities.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserRegistrationService {
    private final UserService userService;
    private final AuthenticationService authenticationService;

    public UserRegistrationService(UserService userService, AuthenticationService authenticationService) {
        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    public User register(User user) {
        String plainPassword = user.getPassword();
        User createdUser = userService.createUser(user);

        AuthenticationRequest authRequest = new AuthenticationRequest(user.getEmail(), plainPassword);
        AuthenticationResponse authResponse = authenticationService.generateToken(authRequest);

        createdUser.setToken(authResponse.getJwtToken());
        createdUser.setModified(LocalDateTime.now());
        createdUser.setLastLogin(LocalDateTime.now());
        userService.updateToken(createdUser);

        return createdUser;
    }
}
