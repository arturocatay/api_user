package com.smartjob.services;

import com.smartjob.dtos.AuthenticationRequest;
import com.smartjob.dtos.AuthenticationResponse;
import com.smartjob.dtos.LoginResponse;
import com.smartjob.persistence.entities.User;
import com.smartjob.services.jwt.UserAuthServiceImpl;
import com.smartjob.utils.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserAuthServiceImpl userDetailsService;
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthenticationService(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                                 UserAuthServiceImpl userDetailsService, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }

    public LoginResponse authenticate(AuthenticationRequest authenticationRequest) {
        User user = userService.findByEmail(authenticationRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("El usuario no fue encontrado!"));

        if (!passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Password incorrecto!");
        }

        AuthenticationResponse authResponse = generateToken(authenticationRequest);
        user.setToken(authResponse.getJwtToken());
        user.setModified(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        userService.updateToken(user);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAuthenticationResponse(authResponse);
        loginResponse.setUser(user);
        return loginResponse;
    }

    public AuthenticationResponse generateToken(AuthenticationRequest authenticationRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationRequest.getEmail(), authenticationRequest.getPassword())
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getEmail());
        String token = jwtUtil.generateToken(userDetails.getUsername());
        AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        authenticationResponse.setJwtToken(token);
        return authenticationResponse;
    }
}