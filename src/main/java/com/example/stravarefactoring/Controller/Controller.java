package com.example.stravarefactoring.Controller;

import com.example.stravarefactoring.ApiAddress;
import com.example.stravarefactoring.DTO.Token;
import com.example.stravarefactoring.DTO.User;
import com.example.stravarefactoring.JsonReader;
import com.example.stravarefactoring.Service.UserService;
import com.example.stravarefactoring.StravaApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

@RestController
@RequiredArgsConstructor
public class Controller {
    private final UserService userService;

    @GetMapping("/request")
    public User userRequest(@RequestParam("code") String code){

        return userService.addUser(code);
    }
}
