package com.example.stravarefactoring;

import com.example.stravarefactoring.Service.KakaoApiClient;
import com.google.maps.model.LatLng;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class TestKakaoApiClient extends KakaoApiClient {

    WebClient webClient = WebClient.builder().build();

    private int count = 0;
    private int max = 1000;
    @Override
    public HashMap<String, List<LinkedHashMap>> api(LatLng latLng) {
        if(count <= max){
            count += 1;
            return super.api(latLng);
        }
        else {
            throw WebClientResponseException.create(429, "test", null, null, null);
        }
    }

    public void initialize(int max){
        count = 0;
        this.max = max;
    }
}
