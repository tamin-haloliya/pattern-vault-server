package com.patternvault.authservice.service;

import com.patternvault.authservice.dto.UserResponse;
import com.patternvault.authservice.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getMe(UUID userId){
        return userRepository.findById(userId).map(UserResponse::from).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
