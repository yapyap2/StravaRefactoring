package com.example.stravarefactoring.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.collection.spi.PersistentSet;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@ToString(exclude = {"location", "rides"})
public class User {

    public User(Token token){

        this.refreshToken = token.getRefresh_token();
        this.accessToken = token.getAccess_token();
        this.rideSeq = 1;
    }

    public User(Token token, UserInfo userInfo, UserStatus userStatus){
        this.refreshToken = token.getRefresh_token();
        this.accessToken = token.getAccess_token();
        this.rideSeq = 1;
        setUserInfo(userInfo);
        setUserStatus(userStatus);
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
        this.weight = info.getWeight();
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
    @Nullable
    private double biggest_ride_distance;
    @Nullable
    private double biggest_climb_elevation_gain;
    @Nullable
    private double totalDistance;
    @Nullable
    private double totalElevation;
    @Nullable
    private double totalTime;
    @Nullable
    private int follower_count;
    @Nullable
    private int friend_count;
    @Nullable
    private LocalDateTime create_at;
    @Nullable
    private LocalDateTime update_at;
    @Nullable
    private String bio;
    @Nullable
    private String country;
    @Nullable
    private String state;
    @Nullable
    private String city;
    @Nullable
    private int totalKudos;
    @Nullable
    private String profile;
    @Nullable
    private double weight;

    @Nullable
    private int avg50;
    @Nullable
    private int avg100;
    @Nullable
    private boolean gosu;

    @Nullable
    private boolean locationComplete = false;

    @Nullable
    private double totalJoule;


//    @Transient
//    private List<Ride> recentRides = new ArrayList<>();

    @JsonIgnore
    @Nullable
    private int rideSeq;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    @Nullable
    private List<Ride> rides = new ArrayList<>();

    public void addRide(List<Ride> rideList){
        rides.addAll(0,rideList);
    }

    @ElementCollection(fetch = FetchType.LAZY)
    @Nullable
    @CollectionTable(name = "LOCATION", joinColumns = @JoinColumn(name = "USER_ID"))
    private Set<String> location = new HashSet<>();


    public void addKudos(int kudos){
        totalKudos += kudos;
    }

    public void addJoule(double joule){ this.totalJoule += joule;}
}
