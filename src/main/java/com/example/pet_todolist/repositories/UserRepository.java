package com.example.pet_todolist.repositories;

import com.example.pet_todolist.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User getUserByLogin(String login);
    Optional<User> findUserByLogin(String login);
}
