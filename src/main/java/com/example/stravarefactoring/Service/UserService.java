package com.example.stravarefactoring.Service;

import com.example.stravarefactoring.Repository.RideRepository;
import com.example.stravarefactoring.Repository.UserJDBCRepository;
import com.example.stravarefactoring.domain.*;
import com.example.stravarefactoring.Repository.UserRepository;
import com.example.stravarefactoring.StravaApiClient;
import com.example.stravarefactoring.exception.NoUpdateDataException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final RideRepository rideRepository;

    private final UserRepository userRepository;
    private final StravaApiClient stravaApiClient;
    private final StravaService stravaService;
    private final ParallelLocationMapper parallelLocationMapper;
    private final LocationQueue locationQueue;

    private final UserJDBCRepository userJDBCRepository;

    @Transactional
    public User addUser(Token token){

        User user;
        UserInfo userInfo = stravaApiClient.getUserInfo(token);
        token.setId(userInfo.getId());

        log.info("incoming request   id : {}", token.getId());


        user = userRepository.findUserById(userInfo.getId());


        if(user != null){
            Hibernate.initialize(user.getRides());
            Hibernate.initialize(user.getLocation());
            user.setAccessToken(token.getAccess_token());
            if(user.getUpdate_at().isBefore(userInfo.getUpdate_at())){
                user.setUserInfo(userInfo);
                user.setUserStatus(stravaApiClient.getUserStatus(token));
            }
            try {
                List<Ride> rideList = stravaService.getRide(user);
                user.setLastUpdated(rideList.get(0).getStart_date_local());
                userRepository.saveAndFlush(user);

                user.addRide(rideList);

                mapping(user, rideList);
                return user;
            } catch (NoUpdateDataException e){
                e.printStackTrace();
                userRepository.save(user);
                return user;
            }
        }
        else {
            user = new User(token, userInfo, stravaApiClient.getUserStatus(token));
        }
        try {
            userRepository.saveAndFlush(user);             // 여기서 user select가 호출되는 이유는 식별자가 Null이 아니라 직접 설정해줬기 때문, 존재하는 식별자인지 한번 확인하는 과정임 Ride도 같이 불러와서 수정해야 함
            List<Ride> rideList = stravaService.getRide(user);
            user.setLastUpdated(rideList.get(0).getStart_date_local());
            userRepository.saveAndFlush(user);

            user.addRide(rideList);

            mapping(user,rideList);
        } catch (NoUpdateDataException e){
            e.printStackTrace();
            userRepository.saveAndFlush(user);
            return user;
        }
        return user;
    }

    public User getUser(int id){
        return userRepository.findUserById(id);
    }

    private void mapping(User user, List<Ride> list){
        CompletableFuture<HashMap<String, Object>> future = parallelLocationMapper.getLocation(list);
        future.thenAccept(result ->{
            if(result.get("status").equals("finish")){
                HashSet<String> resultSet = new HashSet<>((HashSet<String>) result.get("result"));
                resultSet.removeAll(user.getLocation());
                user.getLocation().addAll(resultSet);
                user.setLocationComplete(true);

                log.info("userName : {}    add new Location {}",user.getName(), result.get("result"));
                userJDBCRepository.updateUserWithLocation(user,resultSet);
            }

            else{
                HashSet<String> resultSet = new HashSet<>((HashSet<String>) result.get("result"));

                resultSet.removeAll(user.getLocation());
                user.getLocation().addAll(resultSet);
                result.put("user", user);
                locationQueue.addQueue(result);

                userJDBCRepository.updateUserWithLocation(user,resultSet);
            }
        });
    }


    public void forceMapping(){
        locationQueue.scheduleProcessing();
    }


    public void rebootMapper(){
        List<User> list = userRepository.findAllByLocationCompleteIsFalse();

        list.forEach(u -> {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("user", u);

                    List<Ride> rideList = rideRepository.findAllByUserIdAndMappedFalse(u.getId());
                    map.put("remain", rideList);
                    map.put("result", u.getLocation());
                    locationQueue.addQueue(map);
                    }
                );

        locationQueue.scheduleProcessing();
    }

    @Transactional
    public HashMap<String, Object> getLocation(int userId){
        User user = userRepository.findUserByIdWithLocationEager(userId);
        HashMap<String, Object> map;
        if(user.isLocationComplete()){
            map = new HashMap<>();
            map.put("result", user.getLocation());
        }
        else{
            map = locationQueue.getStatus(userId, user.getRideSeq()-1);
            if(map==null){
                map = new HashMap<>();
                map.put("status", false);
            }else map.put("status", true);
            map.put("result", null);
        } return map;
    }
}