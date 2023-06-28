package com.example.stravarefactoring.Service;

import com.example.stravarefactoring.Repository.RideRepository;
import com.example.stravarefactoring.Repository.UserRepository;
import com.example.stravarefactoring.StravaModifier;
import com.example.stravarefactoring.TestKakaoApiClient;
import com.example.stravarefactoring.config.LocationQueueConfig;
import com.example.stravarefactoring.domain.Token;
import com.example.stravarefactoring.domain.User;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ContextConfiguration;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertTrue;

@SpringBootTest
@Slf4j
@ContextConfiguration(classes = LocationQueueConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserServiceLocationMapperTest {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    @Qualifier("userServiceForQueue")
    UserService userServiceForQueue;

    @Autowired
    @Qualifier("mockQueue")
    LocationQueue locationQueue;

    StravaModifier stravaModifier;

    @Autowired
    UserRepository userRepository;
    @Autowired
    RideRepository rideRepository;

    Token token;

    @BeforeAll
    public void getToken() throws SQLException, ClassNotFoundException {
        stravaModifier = new StravaModifier();
        token = stravaModifier.getToken(1);
    }

    @Test
    public void queueTest() throws InterruptedException {
        LocationQueueConfig config = applicationContext.getBean("locationQueueConfig", LocationQueueConfig.class);
        config.setCount(1);

        User user = userServiceForQueue.addUser(token);

        Thread.sleep(1000);

        User findUser1 = userRepository.findUserById(user.getId());

        assertTrue(user.getName().equals(findUser1.getName()));
        assertTrue(findUser1.getLocation().size() > 0);

        stravaModifier.addRide(token);

        User user2 = userServiceForQueue.addUser(token);

        assertTrue(user2.isLocationComplete());

        locationQueue.scheduleProcessing();

        Thread.sleep(1000);

        User findUser2 = userRepository.findUserById(user2.getId());

        assertTrue(findUser1.getLocation().size() < findUser2.getLocation().size());

        rideRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    public void queueTest2() throws InterruptedException, ExecutionException {
        LocationQueueConfig config = applicationContext.getBean("locationQueueConfig", LocationQueueConfig.class);
        config.setCount(3);

        User user1 = userServiceForQueue.addUser(token);
        awaitTermination();
        User findUser1 = userRepository.findUserById(token.getId());
        assertTrue(findUser1.getLocation().size() > 0);

        stravaModifier.addRide(token);
        User user2 = userServiceForQueue.addUser(token);
        awaitTermination();
        User findUser2 = userRepository.findUserById(token.getId());
        assertTrue(user2.getRides().size() + 1 > user1.getRides().size());
        assertTrue(findUser2.getLocation().size() == findUser1.getLocation().size());

        stravaModifier.addRide(token);
        User user3 = userServiceForQueue.addUser(token);
        awaitTermination();
        User findUser3 = userRepository.findUserById(token.getId());
        assertTrue(user3.getRides().size() + 1 > user1.getRides().size());
        assertTrue(findUser3.getLocation().size() == findUser1.getLocation().size());

        stravaModifier.addRide(token);
        User user4 = userServiceForQueue.addUser(token);
        awaitTermination();
        User findUser4 = userRepository.findUserById(token.getId());
        assertTrue(user4.getRides().size() + 1 > user1.getRides().size());
        assertTrue(findUser4.getLocation().size() == findUser1.getLocation().size());

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            locationQueue.scheduleProcessing();
        });

        future.get();

        awaitTermination();

        User afterUser = userRepository.findUserById(token.getId());

        assertTrue(afterUser.getLocation().size() > findUser4.getLocation().size());
    }


    @Test
    public void kakaoExceptionTest() throws SQLException, ClassNotFoundException {
        token = stravaModifier.getToken(2);

        UserService service = applicationContext.getBean("mockUserServiceKakao", UserService.class);
        TestKakaoApiClient testKakaoApiClient = applicationContext.getBean("testKakaoApiClient", TestKakaoApiClient.class);
        LocationQueue queue = applicationContext.getBean("mockQueueKakao", LocationQueue.class);

        service.addUser(token);
        awaitTermination();

        User user1 = userRepository.findUserById(token.getId());
        assertTrue(user1.getLocation().size() > 0);

        testKakaoApiClient.initialize(1000);

        queue.scheduleProcessing();
        awaitTermination();

        User user2 = userRepository.findUserById(token.getId());
        assertTrue(user2.getLocation().size() > user1.getLocation().size());

    }

    @Test
    public void queueWaitingTest() throws SQLException, ClassNotFoundException {
        token = stravaModifier.getToken(2);

        UserService service = applicationContext.getBean("mockUserServiceKakao", UserService.class);
        TestKakaoApiClient testKakaoApiClient = applicationContext.getBean("testKakaoApiClient", TestKakaoApiClient.class);
        LocationQueue queue = applicationContext.getBean("mockQueueKakao", LocationQueue.class);

        User u1 = service.addUser(token);

        token = stravaModifier.getToken(1);

        User u2 = service.addUser(token);
        awaitTermination();

        User user1 = userRepository.findUserById(u1.getId());
        User user2 = userRepository.findUserById(u2.getId());

        testKakaoApiClient.initialize(3000);
        queue.scheduleProcessing();
        awaitTermination();

        User findUser1 = userRepository.findUserById(user1.getId());
        User findUser2 = userRepository.findUserById(user2.getId());

        assertTrue(findUser1.getLocation().size() > user1.getLocation().size());
        assertTrue(findUser2.getLocation().size() > user2.getLocation().size());







    }



        private void awaitTermination() {
            ThreadPoolTaskExecutor taskExecutor = applicationContext.getBean("MapperAsyncExecutor", ThreadPoolTaskExecutor.class);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            while (true) {
                if (taskExecutor.getActiveCount() == 0) {
                    break;
                }
            }
        }
}
