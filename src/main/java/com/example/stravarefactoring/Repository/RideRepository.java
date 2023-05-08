package com.example.stravarefactoring.Repository;

import com.example.stravarefactoring.DTO.Ride;
import com.example.stravarefactoring.DTO.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long> {

    public List<Ride> findAllByUserId(int id);
}
