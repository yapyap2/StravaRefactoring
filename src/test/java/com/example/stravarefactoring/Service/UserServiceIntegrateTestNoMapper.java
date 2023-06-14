package com.example.stravarefactoring.Service;

import com.example.stravarefactoring.Repository.RideRepository;
import com.example.stravarefactoring.Repository.UserJDBCRepository;
import com.example.stravarefactoring.Repository.UserRepository;
import com.example.stravarefactoring.StravaApiClient;
import com.example.stravarefactoring.StravaModifier;
import com.example.stravarefactoring.config.testConfig;
import com.example.stravarefactoring.domain.Token;
import com.example.stravarefactoring.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.reactive.function.client.WebClient;

import java.sql.SQLException;

import static org.junit.Assert.assertTrue;

@SpringBootTest
@ContextConfiguration(classes = testConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserServiceIntegrateTestNoMapper {

    @Autowired
    ApplicationContext applicationContext;
    Token token;

    @Autowired
    @Qualifier("userServiceMockMapper")
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RideRepository rideRepository;
    @Autowired
    private ParallelLocationMapper locationMapper;
    @Autowired
    private StravaApiClient client;
    StravaModifier stravaModifier;

    @BeforeAll
    public void getToken() throws SQLException, ClassNotFoundException {
        stravaModifier = new StravaModifier();
        token = stravaModifier.getToken(1);
    }

    @Test
    @Transactional
    public void duplicatedUserNoUpdateTest(){

        User user1 = userService.addUser(token);

        awaitTermination();

        User user2 = userService.addUser(token);

        assertTrue(user1.getRides().size() == user2.getRides().size());

    }


    private void awaitTermination() {
        ThreadPoolTaskExecutor taskExecutor = applicationContext.getBean("MapperAsyncExecutor", ThreadPoolTaskExecutor.class);

        while (true) {
            if (taskExecutor.getActiveCount() == 0) {
                break;
            }
        }
    }
}
