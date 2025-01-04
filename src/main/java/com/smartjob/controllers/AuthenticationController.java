package com.smartjob.controllers;

import com.smartjob.dtos.AuthenticationRequest;
import com.smartjob.dtos.AuthenticationResponse;
import com.smartjob.dtos.LoginResponse;
import com.smartjob.persistence.entities.User;
import com.smartjob.services.UserService;
import com.smartjob.services.jwt.UserAuthServiceImpl;
import com.smartjob.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@RequestMapping(path = "/api/auth")
@RestController
public class AuthenticationController {
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserAuthServiceImpl userDetailsService;
    private final UserService userService;

    public AuthenticationController(JwtUtil jwtUtil, AuthenticationManager authenticationManager,UserAuthServiceImpl userDetailsService ,UserService userService) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest authenticationRequest) {
        LoginResponse loginResponse = new LoginResponse();
        ResponseEntity<?> response =null;
        String error = null;

        Optional<User> user = userService.findByEmail(authenticationRequest.getEmail());
            if (user.isPresent()) {
                BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
                if (bCryptPasswordEncoder.matches(authenticationRequest.getPassword(), user.get().getPassword())) {
                    loginResponse.setAuthenticationResponse((AuthenticationResponse)createAuthenticationToken(authenticationRequest).getBody());
                    user.get().setToken(loginResponse.getAuthenticationResponse().getJwtToken());
                    user.get().setModified(LocalDateTime.now());
                    user.get().setLastLogin(LocalDateTime.now());
                    userService.updateToken(user.get());
                    loginResponse.setUser(user.get());
                    response = new ResponseEntity<>(loginResponse, HttpStatus.OK);
                } else {
                    error = "Password incorrecto!";
                    return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
                }
            } else {
                error = "El usuario no fue encontrado!";
                response = new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
            }
        return response;
    }

    @PostMapping(value = "/register")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        ResponseEntity<?> response = null;
        String error = null;
        try {
            String passwordNoEncrypt = user.getPassword();
            User createdUser = userService.createUser(user);
            AuthenticationRequest authenticationRequest = new AuthenticationRequest();
            authenticationRequest.setEmail(user.getEmail());
            authenticationRequest.setPassword(passwordNoEncrypt);
            ResponseEntity<?> authenticationResponse = createAuthenticationToken(authenticationRequest);

            if(authenticationResponse.getStatusCode().equals(HttpStatus.OK)){
                AuthenticationResponse  authentication = (AuthenticationResponse) authenticationResponse.getBody();
                assert authentication != null;
                createdUser.setToken(Objects.requireNonNull(authentication.getJwtToken()));
                createdUser.setModified(LocalDateTime.now());
                createdUser.setLastLogin(LocalDateTime.now());
                userService.updateToken(createdUser);
                response = new ResponseEntity<>(createdUser, HttpStatus.OK);
            } else {
                response = authenticationResponse;
            }

        } catch (IllegalArgumentException iae) {
            error = iae.getMessage();
            if(error.contains("correo") || error.contains("contraseña")) {
                response = new ResponseEntity<>(error, HttpStatus.NOT_ACCEPTABLE);
            } else if (error.contains("registrado")) {
                response = new ResponseEntity<>(error, HttpStatus.IM_USED);
            }
        }
      return response;
    }

    private ResponseEntity<?> createAuthenticationToken(AuthenticationRequest authenticationRequest) {
        AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        ResponseEntity<?> response =null;
        String error = null;

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getEmail(),
                            authenticationRequest.getPassword()));

            UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getEmail());
            authenticationResponse.setJwtToken(jwtUtil.generateToken(userDetails.getUsername()));
            response = new ResponseEntity<>(authenticationResponse, HttpStatus.OK);
        } catch (BadCredentialsException e) {
            error = "Password incorrecto!";
            authenticationResponse.setMensaje(error);
            response = new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        } catch (DisabledException disabledException) {
            error ="Usuario no activo";
            authenticationResponse.setMensaje(error);
            response = new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        } catch (UsernameNotFoundException unfe) {
            error = unfe.getMessage();
            authenticationResponse.setMensaje(error);
            response = new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        return response;
    }

}
