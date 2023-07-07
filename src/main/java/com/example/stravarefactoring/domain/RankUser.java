package com.example.stravarefactoring.domain;

import lombok.Data;

@Data
public class RankUser {

    private String name;

    private String profile;

    private Object field;

    public RankUser(User user){
        this.name = user.getName();
        this.profile = user.getProfile();
    }

}
