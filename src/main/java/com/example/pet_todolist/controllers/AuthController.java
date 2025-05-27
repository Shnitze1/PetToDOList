package com.example.pet_todolist.controllers;


import com.example.pet_todolist.Exeptions.AuthenticationException;
import com.example.pet_todolist.Exeptions.UserAlreadyExistsException;
import com.example.pet_todolist.models.User;
import com.example.pet_todolist.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody User user) {
        try {
            authService.register(user);
            return ResponseEntity.status(HttpStatus.CREATED).body("The user has been registered successfully.");
        }catch (UserAlreadyExistsException exception){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        }catch (IllegalArgumentException exception){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        }catch (Exception exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestHeader("login") String login,
                                        @RequestHeader("password") String password,
                                        HttpServletResponse response) {
        try {
            authService.login(login, password, response);
            return ResponseEntity.status(HttpStatus.OK).body("The user has been login successfully.");
        }catch (IllegalArgumentException | AuthenticationException exception){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        }catch (Exception exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @GetMapping("/refresh")
    public ResponseEntity<String> refresh(HttpServletResponse response,
                                          HttpServletRequest request){
            authService.refresh(request, response);
            return ResponseEntity.ok("Access token refreshed");
    }

}