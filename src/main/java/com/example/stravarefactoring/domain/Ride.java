package com.example.stravarefactoring.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashMap;

@Data
@Entity
@ToString(exclude = {"map" ,"summary_polyline", "user"})
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    private int rideId;

    public String name;
    public double distance;
    public int moving_time;
    public double total_elevation_gain;
    public LocalDateTime start_date_local;

    private int kudos_count;
    private int comment_count;
    private int total_photo_count;
    private double average_watts;
    private double totalWatts;

    private String rideType;

    @Transient
    @JsonIgnore
    public HashMap<String, Object> map;

    @Column(length = 2000)
    private String summary_polyline;

    public double average_speed;

    @Nullable
    public boolean mapped = false;

    private String type;

    @JsonCreator
    public Ride(@JsonProperty("name")String name, @JsonProperty("distance")double distance, @JsonProperty("moving_time")int moving_time, @JsonProperty("total_elevation_gain")double total_elevation_gain, @JsonProperty("start_date_local")LocalDateTime start_date_local, @JsonProperty("map")HashMap<String, Object> map, @JsonProperty("average_speed")double average_speed, @JsonProperty("kudos_count")int kudos_count, @JsonProperty("comment_count")int comment_count, @JsonProperty("total_photo_count")int total_photo_count, @JsonProperty("average_watts")int average_watts
                ,@JsonProperty("type")String type) {
        this.name = name;
        this.distance = distance/ 1000;
        this.moving_time = moving_time;
        this.total_elevation_gain = total_elevation_gain;
        this.start_date_local = start_date_local;
        this.map = map;
        try{
            this.summary_polyline = (String) map.get("summary_polyline");
        } catch(NullPointerException e){
        }
        this.average_speed = average_speed * 3.6;
        this.type = type;


        this.kudos_count =kudos_count;
        this.comment_count = comment_count;
        this.total_photo_count = total_photo_count;
        this.average_watts = average_watts;
        this.totalWatts = average_watts * moving_time;
    }

    public Ride() {

    }
}