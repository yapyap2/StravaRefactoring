package com.example.stravarefactoring;

import com.example.stravarefactoring.DTO.Token;
import com.example.stravarefactoring.DTO.User;
import com.example.stravarefactoring.DTO.UserInfo;
import com.example.stravarefactoring.DTO.UserStatus;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StravaApiClientTest {

    StravaApiClient client = new StravaApiClient();

    Token token;
    UserInfo userInfo;
    UserStatus userStatus;

    @BeforeEach
    public void before() throws IOException {
        client.setWebClient(WebClient.create());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());

        token = objectMapper.readValue(new File("src/main/resources/static/json/Token.json"), Token.class);
        userInfo = objectMapper.readValue(new File("src/main/resources/static/json/UserInfo.json"), UserInfo.class);
        userStatus = objectMapper.readValue(new File("src/main/resources/static/json/UserStatus.json"), UserStatus.class);
    }

    private void mocking(){
        this.client = mock(StravaApiClient.class);

        when(client.getToken(anyString())).thenReturn(token);
        when(client.getUserInfo(any(Token.class))).thenReturn(userInfo);
        when(client.getUserStatus(any(Token.class))).thenReturn(userStatus);
    }

    @Test
    public void StravaApiClientTest(){

        Token token = client.getToken("94c1333f0a2070b91bfb09bf7cbfb2d865d5c1ff");
        User user = new User(token);

        user.setUserInfo(client.getUserInfo(token));
        user.setUserStatus(client.getUserStatus(token));

        System.out.println(user);
    }

    @Test
    public void mockingTest(){
        mocking();

        Token t = client.getToken("fake Code");

        User user = new User(t);
        user.setUserInfo(client.getUserInfo(t));
        user.setUserStatus(client.getUserStatus(t));


        System.out.println(user);
    }
}
