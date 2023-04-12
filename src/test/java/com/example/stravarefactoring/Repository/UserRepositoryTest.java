package com.example.stravarefactoring.Repository;

import com.example.stravarefactoring.DTO.User;
import com.example.stravarefactoring.StravaApiClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    StravaApiClient stravaApiClient;

    @Test
    public void addAndGetTest(){

        User user = new User();
        user.setId(123123);
        user.setName("yapyap");

        userRepository.save(user);

        assertThat(userRepository.getUserById(user.getId()).getName(), is(user.getName()));
    }
}
