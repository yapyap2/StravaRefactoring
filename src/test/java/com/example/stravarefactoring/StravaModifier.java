package com.example.stravarefactoring;

import com.example.stravarefactoring.domain.Token;
import org.springframework.web.reactive.function.client.WebClient;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class StravaModifier {
    WebClient webClient = WebClient.builder().build();
    String dbUrl = "jdbc:mysql://yapdb.c7uknqplmcnk.ap-northeast-2.rds.amazonaws.com:3306/yaptrava";
    String userName = "yapyap";
    String pw = "1712wonwoo";

    public Token getToken() throws SQLException, ClassNotFoundException {
        Token token = new Token();
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


        String sql2 = "update testToken set access_token = ?, refresh_token = ? where id = ?";

        PreparedStatement ps = connection.prepareStatement(sql2);
        ps.setString(1, token.getAccess_token());
        ps.setString(2, token.getRefresh_token());
        ps.setInt(3, id);

        return token;
    }

    public void addRide(Token token){
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        webClient.post()
                .uri("https://www.strava.com/api/v3/activities?name=" + now +"&type=Ride&sport_type=Ride&start_date_local="+ now +"&elapsed_time=1000&distance=10000")
                .header("Authorization", "Bearer " + token.getAccess_token())
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void updateProfile(Token token, Double w){
        webClient.put()
                .uri("https://www.strava.com/api/v3/athlete?weight=" + Double.toString(w))
                .header("Authorization", "Bearer " + token.getAccess_token())
                .retrieve()
                .toBodilessEntity()
                .block();
    }

}
