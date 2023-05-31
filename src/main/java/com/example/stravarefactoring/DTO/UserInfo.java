package com.example.stravarefactoring.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserInfo {

    private int id;
    private String name;
    private LocalDateTime lastUpdated;
    private int follower_count;
    private int friend_count;
    private LocalDateTime create_at;
    private LocalDateTime update_at;
    private String bio;
    private String country;
    private String state;
    private String city;
    private String profile;

    private double weight;
    @JsonCreator
    public UserInfo(@JsonProperty("id") int id, @JsonProperty("firstname")String firstname, @JsonProperty("lastname") String lastname, @JsonProperty("follower_count") int follower_count, @JsonProperty("friend_count")int friend_count,@JsonProperty("created_at") LocalDateTime create_at,@JsonProperty("updated_at") LocalDateTime update_at,@JsonProperty("bio") String bio,@JsonProperty("country") String country,@JsonProperty("state") String state,@JsonProperty("city") String city,@JsonProperty("profile") String profile, @JsonProperty("weight") double weight) {
        this.id = id;
        this.name = firstname + " " + lastname;
        this.follower_count = follower_count;
        this.friend_count = friend_count;
        this.create_at = create_at;
        this.update_at = update_at;
        this.bio = bio;
        this.country = country;
        this.state = state;
        this.city = city;
        this.profile = profile;
        this.weight = weight;
    }
}
