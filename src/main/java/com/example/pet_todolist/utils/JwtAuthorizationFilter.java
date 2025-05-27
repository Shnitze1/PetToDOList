package com.example.pet_todolist.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

public class JwtAuthorizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("Authorization".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        if (request.getRequestURI().equals("/auth/login") || request.getRequestURI().equals("/auth/register") || request.getRequestURI().equals("/auth/refresh")) {
            chain.doFilter(request, response);
            return;
        }
        if (token != null) {
            try {
                Algorithm algorithm = Algorithm.HMAC256("petToDOList");
                DecodedJWT decodedJWT = JWT.require(algorithm)
                        .build()
                        .verify(token);
                String email = decodedJWT.getSubject();

                User userDetails = new User(email, "", Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
                );
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Недействительный или просроченный токен (Filter exp.)");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
