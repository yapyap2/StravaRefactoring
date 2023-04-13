package com.example.stravarefactoring.Service;

import com.example.stravarefactoring.Annotation.RideConstructor;
import com.example.stravarefactoring.DTO.Ride;
import com.example.stravarefactoring.DTO.User;
import com.example.stravarefactoring.Repository.RideRepository;
import com.example.stravarefactoring.StravaApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StravaService {

    private final RideRepository repository;

    private final StravaApiClient client;

    ThreadLocal<Integer> rideSeq = new ThreadLocal<>();

    @RideConstructor
    public List<Ride> getRide(User user){
        int i = 1;
        List<Ride> returnList = new ArrayList<>();

        rideSeq.set(1);


        while (true){
            List<Ride> rideList = client.getRide(user.getAccessToken(), i);
            i++;
            if(rideList.isEmpty()){
                break;
            }
            returnList.addAll(rideList);
        }
        repository.saveAll(returnList);

        return returnList;
    }


    public void setRideSeq(int rideSeq) {
        this.rideSeq.set(rideSeq);
    }

    public int getRideSeq() {
        return rideSeq.get();
    }

}
