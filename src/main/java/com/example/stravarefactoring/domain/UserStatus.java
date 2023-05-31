package com.example.stravarefactoring.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;

@Data
public class UserStatus {

    private double biggest_ride_distance;
    private double biggest_climb_elevation_gain;
    private double totalDistance;
    private double totalElevation;
    private double totalTime;

    @JsonCreator
    public UserStatus(@JsonProperty("biggest_ride_distance") double biggest_ride_distance, @JsonProperty("biggest_climb_elevation_gain") double biggest_climb_elevation_gain, @JsonProperty("all_ride_totals")HashMap<String, Double> map) {
        this.biggest_ride_distance = biggest_ride_distance;
        this.biggest_climb_elevation_gain = biggest_climb_elevation_gain;
        this.totalDistance = map.get("distance");
        this.totalElevation = map.get("elevation_gain");
        this.totalTime = map.get("elapsed_time");
    }
}
