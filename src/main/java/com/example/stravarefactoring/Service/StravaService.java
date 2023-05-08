package com.example.stravarefactoring.Service;

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

    private final StravaApiClient client;

    private final RideRepository rideRepository;

    public List<Ride> getRide(User user){
        int i = 1;
        List<Ride> returnList = new ArrayList<>();
        List<Ride> rideList;
        int rideSeq = user.getRideSeq();
        Boolean check;

        if(user.getLastUpdated() != null){  //lastUpdete 존재(not null) -> 데이터가 존재하는 유저인 경우
            check = checkUpdate(user);
            if(!check){                     //데이터가 서버에 존재하지만 업데이트 할 게 없는 경우
                throw new NoUpdateDataException(user.getName() +" is already have recent data " + user.getLastUpdated().toString());
            }
        } else check = false;               //새로운 유저인경우



        while (true){
            if(check){
                rideList = client.getRideAfter(user.getAccessToken(), i, user.getLastUpdated());
            } else {
                rideList = client.getRide(user.getAccessToken(), i);
            }
            i++;
            for (Ride ride : rideList){
                ride.setRideId(rideSeq);
                rideSeq++;
            }
            if(rideList.isEmpty()){
                break;
            }
            returnList.addAll(rideList);
        }

        user.setRideSeq(rideSeq);

        user.setLastUpdated(returnList.get(0).getStart_date_local());

        rideRepository.saveAll(returnList);

        return returnList;
    }

    public Boolean checkUpdate(User user){

        Ride ride = client.getOneRide(user.getAccessToken()).get(0);

        return user.getLastUpdated().isBefore(ride.getStart_date_local());
    }

}
