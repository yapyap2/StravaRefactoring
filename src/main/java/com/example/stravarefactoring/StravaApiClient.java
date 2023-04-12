package com.example.stravarefactoring;

import com.example.stravarefactoring.DTO.Token;
import com.example.stravarefactoring.DTO.UserInfo;
import com.example.stravarefactoring.DTO.UserStatus;
import org.springframework.web.reactive.function.client.WebClient;

public class StravaApiClient {
    private WebClient webClient;

    public Token getToken(String code){
        return webClient.post().uri(ApiAddress.stravaGetToken(code))
                .retrieve()
                .bodyToMono(Token.class)
                .block();
    }

    public UserInfo getUserInfo(Token token){
        return webClient.get()
                .uri(ApiAddress.ATHLETE_INFO_API)
                .header("Authorization", "Bearer " + token.getAccess_token())
                .retrieve()
                .bodyToMono(UserInfo.class)
                .block();
    }

    public UserStatus getUserStatus(Token token){
        return webClient.get()
                .uri(ApiAddress.ATHLETE_STATUS_API)
                .header("Authorization", "Bearer " + token.getAccess_token())
                .retrieve()
                .bodyToMono(UserStatus.class)
                .block();
    }
    public void setWebClient(WebClient webClient) {
        this.webClient = webClient;
    }
}
