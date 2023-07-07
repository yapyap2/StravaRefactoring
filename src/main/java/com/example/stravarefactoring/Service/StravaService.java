package com.example.stravarefactoring.Service;

import com.example.stravarefactoring.Repository.RideBatchRepository;
import com.example.stravarefactoring.Repository.RideRepository;
import com.example.stravarefactoring.domain.Ride;
import com.example.stravarefactoring.domain.User;
import com.example.stravarefactoring.StravaApiClient;
import com.example.stravarefactoring.exception.NoUpdateDataException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StravaService {

    private final StravaApiClient client;
    private final RideBatchRepository rideBatchRepository;

    private final RideRepository rideRepository;
    public List<Ride> getRide(User  user) throws NoUpdateDataException {
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
            if(rideList.isEmpty()){
                break;
            }
            i++;
            for (Ride ride : rideList){
                ride.setRideId(rideSeq);
                ride.setUser(user);
                rideSeq++;
                verifyingRide(ride, user);
                user.addKudos(ride.getKudos_count());
                user.addJoule(ride.getTotalWatts());
            }
            returnList.addAll(rideList);
        }

        user.setRideSeq(rideSeq);

        return rideBatchRepository.saveAll(returnList);
    }

    public Boolean checkUpdate(User user){

        Ride ride = client.getOneRide(user.getAccessToken()).get(0);

        return user.getLastUpdated().isBefore(ride.getStart_date_local());
    }


    private void verifyingRide(Ride ride, User user){
        double distance = ride.getDistance();
        double elevation = ride.getTotal_elevation_gain();
        double averageSpeed = ride.getAverage_speed();

        if(distance >= 50 && averageSpeed >= 30){
            int avg50 = 0;

            if(averageSpeed < 35){
                avg50 = 1;
            }
            else if(averageSpeed < 40){
                avg50 = 2;
            }
            else avg50 = 3;

            if(user.getAvg50() < avg50) user.setAvg50(avg50);
        }
        if(distance >= 100 && averageSpeed >= 30){
            int avg100 = 0;
            user.setGosu(true);

            if(averageSpeed < 35){
                avg100 = 1;
            }
            if(averageSpeed < 40){
                avg100 = 2;
            }
            else avg100 = 3;

            if(user.getAvg100() < avg100) user.setAvg100(avg100);
        }
    }

}
