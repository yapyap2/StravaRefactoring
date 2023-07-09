package com.example.stravarefactoring.Repository;

import com.example.stravarefactoring.domain.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long> {

    public List<Ride> findAllByUserId(int id);

    public List<Ride> findAllByUserIdAndMappedFalse(int id);

    @Query("select r from Ride r order by r.distance desc limit 5")
    public List<Ride> getTop5Distance();
}
