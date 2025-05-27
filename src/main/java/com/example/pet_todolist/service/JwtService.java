package com.example.pet_todolist.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.pet_todolist.models.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final UserService userService;
    private static final String SECRET_KEY = "petToDOList";
    private static final Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);

    public String generateToken(String login) {
        return JWT.create()
                .withSubject(login)
                .withExpiresAt(new Date(System.currentTimeMillis() + 15 * 60 * 1000))
                .sign(algorithm);
    }

    public String generateRefreshToken(String login) {
        return JWT.create()
                .withSubject(login)
                .withExpiresAt(new Date(System.currentTimeMillis() + 7 * 24 * 3600 * 1000))
                .sign(algorithm);
    }

    public String getTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("Authorization".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public void saveTokenInCookie(String token, HttpServletResponse response) {
        Cookie cookie = new Cookie("Authorization", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(3600);
        response.addCookie(cookie);
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = getTokenFromCookie(request);
        if (accessToken == null || accessToken.isEmpty()){
            log.error("Access token is missing.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is missing.");
        }

        String login;

        try {
            JWT.require(algorithm).build().verify(accessToken);
            log.info("Access token is valid.");
            return;
        } catch (TokenExpiredException e) {
            login = JWT.decode(accessToken).getSubject();
            log.warn("Access token expired for user: {}", login);
        } catch (Exception exception) {
            log.error("Invalid access token: {}", exception.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid access token.");
        }

        User user = userService.getUserByLogin(login);
        if (user == null || user.getRefreshToken() == null){
            log.error("User {} not found or has no refresh token.", login);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found or has no refresh token.");
        }


        try {
            JWT.require(algorithm).build().verify(user.getRefreshToken());
            log.info("Refresh token is valid for user: {}", login);
        } catch (TokenExpiredException e) {
            log.warn("Refresh token expired for user: {}", login);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired.");
        } catch (Exception e) {
            log.error("Invalid refresh token for user {}: {}", login, e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token.");
        }

        String newAccessToken = generateToken(login);
        saveTokenInCookie(newAccessToken, response);
        log.info("New access token generated and saved for user: {}", login);
    }

}

