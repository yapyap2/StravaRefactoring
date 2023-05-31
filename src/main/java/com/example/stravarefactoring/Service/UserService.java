package com.example.stravarefactoring.Service;

import com.example.stravarefactoring.domain.Ride;
import com.example.stravarefactoring.domain.Token;
import com.example.stravarefactoring.domain.User;
import com.example.stravarefactoring.domain.UserInfo;
import com.example.stravarefactoring.Repository.RideRepository;
import com.example.stravarefactoring.Repository.UserRepository;
import com.example.stravarefactoring.StravaApiClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final StravaApiClient stravaApiClient;
    private final StravaService stravaService;
    private final RideRepository rideRepository;

    @Transactional
    public User addUser(Token token){

        User user;
        UserInfo userInfo = stravaApiClient.getUserInfo(token);
        token.setId(userInfo.getId());


        user = userRepository.findUserById(userInfo.getId());

        if(user != null){
            user.setAccessToken(token.getAccess_token());
            if(user.getUpdate_at().isBefore(userInfo.getUpdate_at())){
                user.setUserInfo(userInfo);
                user.setUserStatus(stravaApiClient.getUserStatus(token));
            }
            try {
                List<Ride> rideList = stravaService.getRide(user);
                user.setLastUpdated(rideList.get(0).getStart_date_local());
                user.addRide(rideList);
                userRepository.save(user); // 여기서 user select가 호출되는 이유는 식별자가 Null이 아니라 직접 설정해줬기 때문, 존재하는 식별자인지 한번 확인하는 과정임 Ride도 같이 불러와서 수정해야 함
                return user;
            } catch (NoUpdateDataException e){
                e.printStackTrace();
                userRepository.save(user);
                return user;
            }
        }
        else {
            user = new User(token);
            user.setUserInfo(userInfo);
            user.setUserStatus(stravaApiClient.getUserStatus(token));
        }
        try {
            List<Ride> rideList = stravaService.getRide(user);
            user.setLastUpdated(rideList.get(0).getStart_date_local());
            user.addRide(rideList);
            userRepository.save(user); // 여기서 user select가 호출되는 이유는 식별자가 Null이 아니라 직접 설정해줬기 때문, 존재하는 식별자인지 한번 확인하는 과정임 Ride도 같이 불러와서 수정해야 함
        } catch (NoUpdateDataException e){
            e.printStackTrace();
            userRepository.save(user);
            return user;
        }
         return user;
    }

    public User getUser(int id){
        return userRepository.findUserById(id);
    }
}
