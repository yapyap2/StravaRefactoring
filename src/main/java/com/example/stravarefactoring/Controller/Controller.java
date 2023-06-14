package com.example.stravarefactoring.Controller;

import com.example.stravarefactoring.domain.Token;
import com.example.stravarefactoring.domain.User;
import com.example.stravarefactoring.Service.UserService;
import com.example.stravarefactoring.StravaApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin
public class Controller {
    private final UserService userService;

    private final StravaApiClient client;

    @GetMapping("/request")
    public User userRequest(@RequestParam("code") String code){

        Token token = client.getToken(code);

        return userService.addUser(token);
    }
}
