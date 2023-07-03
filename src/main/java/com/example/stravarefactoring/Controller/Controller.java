package com.example.stravarefactoring.Controller;

import com.example.stravarefactoring.TestKakaoApiClient;
import com.example.stravarefactoring.domain.Token;
import com.example.stravarefactoring.domain.User;
import com.example.stravarefactoring.Service.UserService;
import com.example.stravarefactoring.StravaApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
public class Controller {
    private final UserService userService;

    private final StravaApiClient client;

    @GetMapping("/request")
    public User userRequest(@RequestParam("code") String code){

        Token token = client.getToken(code);

        return userService.addUser(token);
    }

    @GetMapping("/mapping")
    public void mapping(){
        userService.forceMapping();
        log.info("force mapping started");
    }

    @GetMapping("/getLocation")
    public HashMap<String, Object> getLocation(@RequestParam int id){
        return userService.getLocation(id);
    }
}
