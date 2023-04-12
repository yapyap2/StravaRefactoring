package com.example.stravarefactoring.LearningTest;

import com.example.stravarefactoring.ApiAddress;
import com.example.stravarefactoring.DTO.Token;
import com.example.stravarefactoring.DTO.User;
import com.example.stravarefactoring.DTO.UserInfo;
import com.example.stravarefactoring.DTO.UserStatus;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

public class StravaApiLearningTest {
    Token token;
    UserInfo userInfo;
    UserStatus userStatus;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    WebClient.Builder mockWebClientBuilder;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    WebClient mockWebClient;

    @BeforeEach
    public void before() throws IOException {
        MockitoAnnotations.openMocks(this);

        mockWebClient = mockWebClientBuilder.baseUrl(anyString()).build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());

        token = objectMapper.readValue(new File("src/main/resources/static/json/Token.json"), Token.class);
        userInfo = objectMapper.readValue(new File("src/main/resources/static/json/UserInfo.json"), UserInfo.class);
        userStatus = objectMapper.readValue(new File("src/main/resources/static/json/UserStatus.json"), UserStatus.class);


        given(mockWebClient.post()
                .uri(ApiAddress.stravaGetToken("code"))
                .retrieve()
                .bodyToMono(Token.class)
                .block()).willReturn(token);

        given(mockWebClient.get()
                .uri(ApiAddress.ATHLETE_INFO_API)
                .header("Authorization", "Bearer " + token.getAccess_token())
                .retrieve()
                .bodyToMono(UserInfo.class)
                .block()).willReturn(userInfo);

        given(mockWebClient.get()
                .uri(ApiAddress.ATHLETE_STATUS_API)
                .header("Authorization", "Bearer " + token.getAccess_token())
                .retrieve().bodyToMono(UserStatus.class)
                .block()).willReturn(userStatus);


    }

    @Test
    public void mockWebClientTest(){

        Token t = mockWebClient.post().uri(ApiAddress.stravaGetToken("code"))
                .retrieve()
                .bodyToMono(Token.class)
                .block();

        User user = new User(t);

        UserInfo info = mockWebClient.get()
                .uri(ApiAddress.ATHLETE_INFO_API)
                .header("Authorization", "Bearer " + token.getAccess_token())
                .retrieve()
                .bodyToMono(UserInfo.class)
                .block();

        user.setUserInfo(info);

        UserStatus status = mockWebClient.get()
                .uri(ApiAddress.ATHLETE_STATUS_API)
                .header("Authorization", "Bearer " + token.getAccess_token())
                .retrieve()
                .bodyToMono(UserStatus.class)
                .block();

        user.setUserStatus(status);


        System.out.println(user);

    }



//    RestTemplate template = new RestTemplate();
//
//    @Test
//    public void httpTest(){
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        String url = ApiAddress.stravaGetToken("3f8092f743377e63b827a2c5b4ad13de79b106dc");
//
//        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
//
//        ResponseEntity<Token> responseEntity = template.exchange(url, HttpMethod.POST, requestEntity, Token.class);
//
//        Token token = responseEntity.getBody();
//
//
//        User user = new User(token);
//
//        headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + token.getAccess_token());
//
//        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
//        UserInfo userInfo = template.exchange(ApiAddress.ATHLETE_INFO_API, HttpMethod.GET, httpEntity, UserInfo.class).getBody();
//
//        System.out.println(userInfo);
//        user.setUserInfo(userInfo);
//
//        UserStatus userStatus = template.exchange(ApiAddress.athleteStatusApi(user.getId()), HttpMethod.GET, httpEntity, UserStatus.class).getBody();
//
//        System.out.println(userStatus);
//        user.setUserStatus(userStatus);
//
//
//        System.out.println(user);
//    }


    WebClient webClient = WebClient.builder().baseUrl("https://www.strava.com").build();

    @Test
    public void webClientTest(){


        Token token = webClient.post().uri(ApiAddress.stravaGetToken("87acb0ac63119903de99595af6f11f284cc7f71e"))
                .retrieve().bodyToMono(Token.class).block();

        User user = new User(token);

        UserInfo userInfo = webClient.get().uri(ApiAddress.ATHLETE_INFO_API)
                                .header("Authorization", "Bearer " + token.getAccess_token())
                                .retrieve().bodyToMono(UserInfo.class).block();

        user.setUserInfo(userInfo);

        UserStatus userStatus = webClient.get().uri(ApiAddress.ATHLETE_STATUS_API)
                                    .header("Authorization", "Bearer " + token.getAccess_token())
                                   .retrieve().bodyToMono(UserStatus.class).block();

        user.setUserStatus(userStatus);

        System.out.println(user);
    }
}
