package com.example.stravarefactoring.Service;

import com.example.stravarefactoring.DTO.Ride;
import com.example.stravarefactoring.DTO.Token;
import com.example.stravarefactoring.DTO.User;
import com.example.stravarefactoring.DTO.UserInfo;
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

    @Transactional
    public User addUser(String code){
        Token token = stravaApiClient.getToken(code);

        User user;
        UserInfo userInfo = stravaApiClient.getUserInfo(token);

        user = userRepository.findUserById(userInfo.getId());

        if(user != null){
            user.setAccessToken(token.getAccess_token());
            if(user.getUpdate_at().isBefore(userInfo.getUpdate_at())){
                user.setUserInfo(userInfo);

                user.setUserStatus(stravaApiClient.getUserStatus(token));
            }
        }
        else {
            user = new User(token);
            user.setUserInfo(userInfo);
            user.setUserStatus(stravaApiClient.getUserStatus(token));
        }
        try {
            List<Ride> rideList = stravaService.getRide(user);
            userRepository.save(user);

            user.addRide(rideList);
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
