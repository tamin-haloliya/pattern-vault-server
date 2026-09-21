package com.patternvault.authservice.service;

import com.patternvault.authservice.dto.RegisterRequest;
import com.patternvault.authservice.dto.RegisterResponse;
import com.patternvault.authservice.entity.User;
import com.patternvault.authservice.exception.ConflictException;
import com.patternvault.authservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ConflictException("Email already exists.");
        }

        if(userRepository.existsByUsername(req.username())){
            throw new ConflictException("Username already taken.");
        }

        User user = new User(req.username(), req.email(), passwordEncoder.encode(req.password()));
        User saved = userRepository.save(user);

        return new RegisterResponse(saved.getId(), saved.getUsername(), saved.getEmail(), saved.getCreatedAt());
    }
}
