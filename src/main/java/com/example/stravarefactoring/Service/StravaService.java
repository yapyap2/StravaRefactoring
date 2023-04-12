package com.example.stravarefactoring.Service;

import com.example.stravarefactoring.DTO.Ride;
import com.example.stravarefactoring.DTO.User;
import com.example.stravarefactoring.Repository.RideRepository;
import com.example.stravarefactoring.StravaApiClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StravaService {

    RideRepository repository;

    StravaApiClient client;

    public List<Ride> getRide(User user){
        int i = 1;
        List<Ride> returnList = new ArrayList<>();

        while (true){
            List<Ride> rideList = client.getRide(user.getAccessToken(), i);
            if(rideList.isEmpty()){
                break;
            }
            returnList.addAll(rideList);
        }
        repository.saveAll(returnList);

        return returnList;
    }

    public void setClient(StravaApiClient client) {
        this.client = client;
    }

    public void setRepository(RideRepository repository) {
        this.repository = repository;
    }
}
