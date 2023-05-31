package com.example.stravarefactoring.Service;

import com.example.stravarefactoring.DTO.Ride;
import com.example.stravarefactoring.DTO.Token;
import com.example.stravarefactoring.DTO.User;
import com.example.stravarefactoring.Repository.RideRepository;
import com.example.stravarefactoring.Repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.reactive.function.client.WebClient;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserStravaIntergrateTest {
    String dbUrl = "jdbc:mysql://yapdb.c7uknqplmcnk.ap-northeast-2.rds.amazonaws.com:3306/yaptrava";
    String userName = "yapyap";
    String pw = "1712wonwoo";
    Token token = new Token();

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RideRepository rideRepository;

    WebClient webClient = WebClient.builder().build();

    @BeforeAll
    public void getToken() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");

        Connection connection = DriverManager.getConnection(dbUrl, userName, pw);

        String sql1 = "SELECT refresh_token, id from testToken";

        Statement stateMent = connection.createStatement();
        ResultSet resultSet = stateMent.executeQuery(sql1);

        resultSet.next();

        String refreshToken = resultSet.getString("refresh_token");
        int id = resultSet.getInt("id");

        resultSet.close();
        stateMent.close();

        HashMap<String, String> map = webClient.post()
                .uri("https://www.strava.com/oauth/token?client_id=89942&client_secret=678d9e347c605e0d6a705b19db49d4ba379c9748&refresh_token=" + refreshToken +"&grant_type=refresh_token")
                .retrieve()
                .bodyToMono(HashMap.class)
                .block();

        token.setAccess_token(map.get("access_token"));
        token.setRefresh_token(map.get("refresh_token"));


        String sql2 = "update testToken set refresh_token = ? where id = ?";

        PreparedStatement ps = connection.prepareStatement(sql2);
        ps.setString(1, map.get("refresh_token"));
        ps.setInt(2, id);

        ps.executeUpdate();
    }


    @Test
    @Transactional
    public void newUserTest(){

        User user = userService.addUser(token);

        User findUser  = userRepository.findUserById(token.getId());

        assertTrue(findUser != null);
        assertTrue(user.getRides().size() == rideRepository.findAllByUserId(user.getId()).size());

    }

    @Autowired
    EntityManager entityManager;

    @Test
    @Transactional
    public void duplicatedUserProfileUpdate(){
        User user1 = userService.addUser(token);

        User u = entityManager.find(User.class, user1.getId());
        List<Ride> r = u.getRides();

        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        webClient.post()
                .uri("https://www.strava.com/api/v3/activities?name=" + now +"&type=Ride&sport_type=Ride&start_date_local="+ now +"&elapsed_time=1000&distance=10000")
                .header("Authorization", "Bearer " + token.getAccess_token())
                .retrieve()
                .toBodilessEntity()
                .block();

        User user2 = userService.addUser(token);

        assertTrue(user2.getRides().size() == user1.getRides().size() + 1);
        assertTrue(user2.getLastUpdated().isAfter(user1.getLastUpdated()));
        assertTrue(rideRepository.findAllByUserId(user2.getId()).size() == user1.getRides().size() + 1);

    }


}
