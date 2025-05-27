package com.example.pet_todolist.service;

import com.example.pet_todolist.Exeptions.AuthenticationException;
import com.example.pet_todolist.models.User;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;


    public void register(User user) {
        userService.registrationNewUser(user);
    }


    public void login(String login, String password, HttpServletResponse response) {
        if (StringUtils.isBlank(login) || StringUtils.isBlank(password)){
            throw new IllegalArgumentException("Login and password must not be empty");
        }

        boolean isAuthenticated = userService.authenticateUser(login, password);

        if (!isAuthenticated){
            throw new AuthenticationException("Invalid login or password");
        }

        String accessToken = jwtService.generateToken(login);
        String refreshToken = jwtService.generateRefreshToken(login);

        User user = userService.getUserByLogin(login);
        user.setRefreshToken(refreshToken);
        userService.updateUser(user);

        jwtService.saveTokenInCookie(accessToken, response);
    }

    public void refresh(HttpServletRequest request, HttpServletResponse response) {
        jwtService.refreshToken(request, response);
    }
}
