package com.example.stravarefactoring.Repository;

import com.example.stravarefactoring.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    User findUserById(int id);

    @EntityGraph(attributePaths = {"rides", "location"})
    List<User> findAllByLocationCompleteIsTrue();


    @Query("select u from User u left join fetch u.location where u.id = :userId")
    User findUserByIdWithLocationEager(@Param("userId") int userId);

    List<User> findAllByLocationCompleteIsFalse();

    @Query("select u from User u order by u.totalDistance desc limit 5")
    List<User> top5Distance();

    @Query("select u from User u order by u.totalElevation desc limit 5")
    List<User> top5Elevation();

    @Query("select u from User u order by ((u.totalDistance/100) / u.totalElevation) desc limit 5")
    List<User> top5Climber();
}