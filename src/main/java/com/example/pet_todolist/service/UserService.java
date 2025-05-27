package com.example.pet_todolist.service;

import com.example.pet_todolist.Exeptions.UserAlreadyExistsException;
import com.example.pet_todolist.models.User;
import com.example.pet_todolist.repositories.UserRepository;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registrationNewUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be empty.");
        }

        String login = user.getLogin();
        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException("Login cannot be empty");
        }

        Optional<User> existingUser = userRepository.findUserByLogin(login);
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException("A user with login '" + login + "' is already registered");
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        userRepository.save(user);
        log.info("User with login '{}' successfully registered", login);
    }

    public boolean authenticateUser(String login, String password) {
        if (StringUtils.isBlank(login) || StringUtils.isBlank(password)){
            log.warn("Empty credentials provided for authentication");
            throw new IllegalArgumentException("Login and password must not be empty");
        }

        Optional<User> userOptional = userRepository.findUserByLogin(login);

        if (userOptional.isEmpty()){
            log.warn("Failed authentication attempt for user '{}'", login);
            return false;
        }

        User user = userOptional.get();

        boolean isAuthenticated = passwordEncoder.matches(password, user.getPassword());

        if (isAuthenticated) {
            log.info("User '{}' successfully authenticated", login);
        } else {
            log.warn("Failed authentication attempt for user '{}'", login);
        }

        return isAuthenticated;
    }

    public User getUserByLogin(String login){
        return userRepository.getUserByLogin(login);
    }

    public void updateUser(User user){
        userRepository.save(user);
    }

}
