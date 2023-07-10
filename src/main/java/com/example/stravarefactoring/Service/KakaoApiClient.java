package com.example.stravarefactoring.Service;

import com.google.maps.model.LatLng;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@Component
public class KakaoApiClient {

    WebClient webClient = WebClient.builder().build();
    public HashMap<String, List<LinkedHashMap>> api(LatLng latLng){

        try{
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        HashMap<String, List<LinkedHashMap>> returnData = webClient.get()
                .uri("https://dapi.kakao.com/v2/local/geo/coord2address?x=" + latLng.lng + "&y=" + latLng.lat)
                .header("Authorization", "KakaoAK 8b28f1050844ddb7dbf7c11bd77d959e")
                .retrieve()
                .bodyToMono(HashMap.class)
                .block();

        return returnData;
    }
}
