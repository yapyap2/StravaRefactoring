package com.example.stravarefactoring.Repository;

import com.example.stravarefactoring.DTO.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    public User getUserById(int id);
}
