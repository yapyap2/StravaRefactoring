package com.example.stravarefactoring;

import com.example.stravarefactoring.DTO.UserStatus;

public class ApiAddress {

    public static String stravaGetToken(String code){
       return "https://www.strava.com/oauth/token?client_id=89942&client_secret=678d9e347c605e0d6a705b19db49d4ba379c9748&code=" + code + "&grant_type=authorization_code";
    }
    public static String athleteStatusApi(Integer id){
        return "https://www.strava.com/api/v3/athletes/" + id.toString() + "/stats";
    }
    public static final String GET_RIDE_1 = "https://www.strava.com/api/v3/athlete/activities?per_page=1";
    public static final String ATHLETE_INFO_API = "https://www.strava.com/api/v3/athlete";
    public static final String ATHLETE_STATUS_API = "https://www.strava.com/api/v3/athletes/15580355/stats";
    public static final String GET_RIDE_100 = "https://www.strava.com/api/v3/athlete/activities?per_page=100&page=";
}
