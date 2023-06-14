package com.example.stravarefactoring.Repository;

import com.example.stravarefactoring.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserJDBCRepository {

    private final JdbcTemplate jdbcTemplate;

    public void save(User user){

        String sql = "insert into " +
                "user" +
                "(access_token, avg100, avg50, biggest_climb_elevation_gain, biggest_ride_distance, bio, city," +
                "country, create_at, follower_count, friend_count, gosu, last_updated, name, profile, refresh_token, ride_seq," +
                " state, total_distance, total_elevation, total_kudos, total_time, update_at, weight, id) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql, user.getAccessToken(), user.getAvg100(), user.getAvg50(), user.getBiggest_climb_elevation_gain(),
                user.getBiggest_ride_distance(), user.getBio(), user.getCity(), user.getCountry(),user.getCreate_at(), user.getFollower_count(), user.getFriend_count(),
                user.isGosu(), user.getLastUpdated(), user.getName(), user.getProfile(), user.getRefreshToken(), user.getRideSeq(),
                user.getState(), user.getTotalDistance(), user.getTotalElevation(), user.getTotalKudos(), user.getTotalTime(), user.getUpdate_at(), user.getWeight(), user.getId()
        );


    }

}
