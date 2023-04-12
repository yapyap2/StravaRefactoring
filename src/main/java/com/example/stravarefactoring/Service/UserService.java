package com.example.stravarefactoring.Service;

import com.example.stravarefactoring.DTO.Token;
import com.example.stravarefactoring.DTO.User;
import com.example.stravarefactoring.Repository.UserRepository;
import com.example.stravarefactoring.StravaApiClient;
import org.springframework.stereotype.Service;


@Service
public class UserService {

    private UserRepository userRepository;
    private StravaApiClient stravaApiClient;

    public void addUser(String code){

        Token token = stravaApiClient.getToken(code);

        User user = new User(token);

        user.setUserInfo(stravaApiClient.getUserInfo(token));
        user.setUserStatus(stravaApiClient.getUserStatus(token));

        userRepository.save(user);
    }

    public User getUser(int id){
        return userRepository.getUserById(id);
    }
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void setStravaApiClient(StravaApiClient stravaApiClient) {
        this.stravaApiClient = stravaApiClient;
    }
}
