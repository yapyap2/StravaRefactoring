package com.example.stravarefactoring.Repository;

import com.example.stravarefactoring.DTO.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RideRepository extends JpaRepository<Ride, Long> {
}
