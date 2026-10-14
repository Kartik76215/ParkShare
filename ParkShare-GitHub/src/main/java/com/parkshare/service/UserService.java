package com.parkshare.service;

import com.parkshare.dto.LoginRequest;
import com.parkshare.dto.RegisterRequest;
import com.parkshare.dto.UserResponse;
import com.parkshare.entity.User;
import com.parkshare.exception.BadRequestException;
import com.parkshare.exception.ResourceNotFoundException;
import com.parkshare.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public UserService(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    public UserResponse register(RegisterRequest request) {
        if (request.getPassword() == null || request.getPassword().length() < 4) {
            throw new BadRequestException("Password must be at least 4 characters.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered.");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordService.hash(request.getPassword()));
        user.setRole(request.getRole());
        return new UserResponse(userRepository.save(user));
    }

    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password."));
        if (!passwordService.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password.");
        }
        if (!passwordService.isHashed(user.getPassword())) {
            user.setPassword(passwordService.hash(request.getPassword()));
            userRepository.save(user);
        }
        return new UserResponse(user);
    }

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }
}
