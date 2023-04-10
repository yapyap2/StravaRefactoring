package com.example.stravarefactoring.DTO;

import lombok.Data;

import java.util.Map;

@Data
public class Token {
    public String refresh_token;
    public String access_token;
}
