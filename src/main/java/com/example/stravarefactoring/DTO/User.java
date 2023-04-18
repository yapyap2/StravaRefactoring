package com.example.stravarefactoring.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class User {

    public User(Token token){

        this.refreshToken = token.getRefresh_token();
        this.accessToken = token.getAccess_token();
        this.rideSeq = 1;
    }

    public void setUserInfo(UserInfo info){
        this.bio = info.getBio();
        this.id = info.getId();
        this.name = info.getName();
        this.city = info.getCity();
        this.country = info.getCountry();
        this.state = info.getState();
        this.create_at = info.getCreate_at();
        this.follower_count = info.getFollower_count();
        this.friend_count = info.getFriend_count();
        this.update_at = info.getUpdate_at();
        this.profile = info.getProfile();
    }

    public void setUserStatus(UserStatus status){
        this.biggest_climb_elevation_gain = status.getBiggest_climb_elevation_gain();
        this.biggest_ride_distance = status.getBiggest_ride_distance();
        this.totalDistance = status.getTotalDistance();
        this.totalElevation = status.getTotalElevation();
        this.totalTime = status.getTotalTime();
    }

    private String refreshToken;
    private String accessToken;

    @Id
    private int id;
    private String name;
    private LocalDateTime lastUpdated;
    private double biggest_ride_distance;
    private double biggest_climb_elevation_gain;
    private double totalDistance;
    private double totalElevation;
    private double totalTime;
    private int follower_count;
    private int friend_count;
    private LocalDateTime create_at;
    private LocalDateTime update_at;
    private String bio;
    private String country;
    private String state;
    private String city;
    private int totalKudos;
    private String profile;

    private String avg50;
    private String avg100;
    private boolean gosu;


//    @Transient
//    private List<Ride> recentRides = new ArrayList<>();

    @JsonIgnore
    private int rideSeq;

    @OneToMany
    private List<Ride> rides = new ArrayList<>();

    public void addRide(List<Ride> rideList){
        rides.addAll(0,rideList);
    }
//
//    @OneToMany(fetch = FetchType.EAGER, mappedBy = "userId")
//    @Fetch(FetchMode.SELECT)
//    private List<Location> location = new ArrayList<>();
//
//    @Transient
//    private List<String> strLocation = new ArrayList<>();
}
