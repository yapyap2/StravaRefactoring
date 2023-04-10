package com.example.stravarefactoring.LearningTest;

import com.example.stravarefactoring.ApiAddress;
import com.example.stravarefactoring.DTO.Token;
import com.example.stravarefactoring.DTO.User;
import com.example.stravarefactoring.DTO.UserInfo;
import com.example.stravarefactoring.DTO.UserStatus;
import org.junit.Test;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class StravaApiLearningTest {

    String accessToken = "2143f7bfd631f3d634737ed54d4f7245a75528bf";

    RestTemplate template = new RestTemplate();

    WebClient webClient = WebClient.builder().baseUrl("https://www.strava.com").build();

    @Test
    public void httpTest(){

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String url = ApiAddress.stravaGetToken("9489c6274cd33ae1c5e28ba399d4e6e2a9f5bd60");

        System.out.println(url);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Token> responseEntity = template.exchange(url, HttpMethod.POST, requestEntity, Token.class);

        Token token = responseEntity.getBody();

        System.out.println(token);

        User user = new User(token);

        headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token.getAccess_token());

        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        UserInfo userInfo = template.exchange(ApiAddress.ATHLETE_INFO_API, HttpMethod.GET, httpEntity, UserInfo.class).getBody();

        user.setUserInfo(userInfo);

        UserStatus userStatus = template.exchange(ApiAddress.athleteStatusApi(user.getId()), HttpMethod.GET, httpEntity, UserStatus.class).getBody();

        user.setUserStatus(userStatus);


        System.out.println(user);
    }

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
