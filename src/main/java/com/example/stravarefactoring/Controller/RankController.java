package com.example.stravarefactoring.Controller;

import com.example.stravarefactoring.Service.RankService;
import com.example.stravarefactoring.domain.RankRide;
import com.example.stravarefactoring.domain.RankUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
@RequiredArgsConstructor
public class RankController {

    private final RankService rankService;

    @GetMapping("/distance")
    public List<RankUser> getTop5Distance(){
        return rankService.getTop5Distance();
    }

    @GetMapping("/elevation")
    public List<RankUser> getTop5Elevation(){
        return rankService.getTop5Elevation();
    }

    @GetMapping("/rideDistance")
    public List<RankRide> getTop5DistanceRide(){
        return rankService.getTop5DistanceRide();
    }

    @GetMapping("/climber")
    public List<RankUser> getTop5Climber(){
        return rankService.getTop5Climber();
    }
}
