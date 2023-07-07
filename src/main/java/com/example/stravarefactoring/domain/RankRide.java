package com.example.stravarefactoring.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class RankRide {
    private String rideName;
    private String date;
    private double distance;
    private double elevation;
    private double avgSpeed;

    private String userName;
    private String userProfile;

    public RankRide(Ride ride){
        this.rideName = ride.getName();
        this.date = ride.getStart_date_local().format(DateTimeFormatter.ISO_DATE);
        this.distance = ride.getDistance();
        this.elevation = ride.getTotal_elevation_gain();
        this.avgSpeed = ride.getAverage_speed();

        this.userName = ride.getUser().getName();
        this.userProfile = ride.getUser().getProfile();
    }
}
