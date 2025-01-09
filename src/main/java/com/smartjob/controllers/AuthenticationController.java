package com.smartjob.controllers;

import com.smartjob.dtos.AuthenticationRequest;
import com.smartjob.dtos.LoginResponse;
import com.smartjob.persistence.entities.User;
import com.smartjob.process.ProcessRequest;
import com.smartjob.services.AuthenticationService;
import com.smartjob.services.UserRegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(path = "/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserRegistrationService userRegistrationService;
    private final ProcessRequest processRequest;

    public AuthenticationController(AuthenticationService authenticationService, UserRegistrationService userRegistrationService, ProcessRequest processRequest) {
        this.authenticationService = authenticationService;
        this.userRegistrationService = userRegistrationService;
        this.processRequest = processRequest;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest authenticationRequest) {
        return processRequest.process(() -> {
            LoginResponse loginResponse = authenticationService.authenticate(authenticationRequest);
            return new ResponseEntity<>(loginResponse, HttpStatus.OK);
        });
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        return processRequest.process(() -> {
            User registeredUser = userRegistrationService.register(user);
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        });
    }
}
