package com.example.stravarefactoring.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Token {
    public String refresh_token;
    public String access_token;

    int id;

    @JsonCreator
    public Token(@JsonProperty("refresh_token")String refresh_token, @JsonProperty("access_token")String access_token){
        this.access_token = access_token;
        this.refresh_token = refresh_token;
    }

    public Token() {

    }
}
