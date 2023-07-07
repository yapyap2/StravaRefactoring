package com.example.stravarefactoring.Repository;

import com.example.stravarefactoring.Service.UserService;
import com.example.stravarefactoring.StravaModifier;
import com.example.stravarefactoring.domain.User;
import com.example.stravarefactoring.StravaApiClient;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.transaction.Transaction;
import jakarta.transaction.Transactional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;


import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    StravaApiClient stravaApiClient;
    StravaModifier stravaModifier = new StravaModifier();
    @Autowired
    UserService userService;

    @Test
    public void addAndGetTest(){

        User user = new User();
        user.setId(123123);
        user.setName("yapyap");

        userRepository.save(user);

        assertThat(userRepository.findUserById(user.getId()).getName(), is(user.getName()));
    }

    @Autowired
    PlatformTransactionManager transactionManager;

    @Test
    public void fetchLoadTest() throws SQLException, ClassNotFoundException {

        User u = userService.addUser(stravaModifier.getToken(1));

        awaitTermination();

        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                User findUser1 = userRepository.findUserById(u.getId());

                findUser1.getLocation().size();
                System.out.println(findUser1.getRides().get(0).getName());
            }
        });

        rideRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }


    @Autowired
    EntityManager entityManager;
    @Test
    public void entityGraphFindTest() throws SQLException, ClassNotFoundException {
        User u = userService.addUser(stravaModifier.getToken(1));
        awaitTermination();
        entityManager.clear();

        User u2 = userRepository.findUserById(u.getId());

        assertTrue(u2.getRides().size() > 0);

    }


    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    private RideRepository rideRepository;

    private void awaitTermination() {
        ThreadPoolTaskExecutor taskExecutor = applicationContext.getBean("MapperAsyncExecutor", ThreadPoolTaskExecutor.class);

        while (true) {
            if (taskExecutor.getActiveCount() == 0) {
                break;
            }
        }
    }


}
