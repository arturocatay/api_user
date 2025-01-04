package com.smartjob.services;

import com.smartjob.persistence.entities.User;
import com.smartjob.persistence.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Value("${app.password.regex}")
    private String passwordRegex;
    @Value("${app.email.regex}")
    private String emailRegex;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        if (!Pattern.matches(emailRegex, user.getEmail())) {
            throw new IllegalArgumentException("El correo tiene un formato inválido.");
        }

        if (!Pattern.matches(passwordRegex, user.getPassword())) {
            throw new IllegalArgumentException("La contraseña tiene un formato inválido.");
        }

        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("El correo ya está registrado.");
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void updateToken(User user){
        userRepository.updateTokenUserByEmail(user.getEmail(), user.getToken(), user.getModified(), user.getLastLogin());
    }
}
