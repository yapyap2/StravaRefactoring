package com.example.stravarefactoring.Service;

import com.example.stravarefactoring.domain.Ride;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.LatLng;
import io.netty.util.internal.StringUtil;
import jakarta.persistence.Index;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class LocationMapper {

    WebClient webClient = WebClient.builder().build();

    @Async("MapperAsyncExecutor")
    public CompletableFuture<HashSet<String>> getLocation(List<Ride> rideList){
        int userId = rideList.get(0).getUser().getId();
        log.info("{} mapper running. userID : {}", Thread.currentThread().getName(), rideList.get(0).getId());

        HashSet<String> returnSet = new HashSet<>();

        for(Ride ride : rideList){
            String polyline = ride.getSummary_polyline();
            if(polyline.equals("")) continue;
            HashSet<String> location;
            try {
                location = getAddress(polyline);
            } catch (RuntimeException e){
                e.printStackTrace();
                continue;
            }


            returnSet.addAll(location);
            log.info("{} end", ride.getName());
        }
        return CompletableFuture.completedFuture(returnSet);
    }


    public HashSet<String> getAddress(String polyline){

        HashSet<String> hashSet = new HashSet<>();

        List<LatLng> latLngList = decode(polyline);
        latLngList = calculateAvg(latLngList);

        for(LatLng latLng : latLngList){
            HashMap<String, List<LinkedHashMap>> returnData = webClient.get()
                    .uri("https://dapi.kakao.com/v2/local/geo/coord2address?x=" + latLng.lng + "&y=" + latLng.lat)
                    .header("Authorization", "KakaoAK 8b28f1050844ddb7dbf7c11bd77d959e")
                    .retrieve()
                    .bodyToMono(HashMap.class)
                    .block();
            LinkedHashMap<String, HashMap> document = new LinkedHashMap<>();
            try{

                document = returnData.get("documents").get(0);

            }catch (RuntimeException e){
                continue;
            }

            HashMap<String, String> address = document.get("address");

            hashSet.add(address.get("region_2depth_name"));
        }
        return hashSet;
    }

    public List<LatLng> decode(String polyline){
        String newEncodedString = polyline.replace("\\\\", "\\");
        List<LatLng> decoded;
        try{
            decoded = PolylineEncoding.decode(newEncodedString);
        } catch (StringIndexOutOfBoundsException e){
            throw new RuntimeException(e);
        }

        return decoded;
    }

    public List<LatLng> calculateAvg(List<LatLng> list){

        List<LatLng> avgLatLng = new ArrayList<>();

        int window = 5;

        for(int i = 0; i <= list.size() + 1 -window; i += window){
            List<LatLng> subList;
            int counter;
            try {
                 subList = list.subList(i, i + window);
                 counter = window;
            } catch (IndexOutOfBoundsException e){
                subList = list.subList(i, list.size());
                counter = list.size() + 1 - i;
            }
            LatLng avg = new LatLng();
            double lat = 0;
            double lng = 0;
            for (LatLng latLng : subList) {
                lat += latLng.lat;
                lng += latLng.lng;
            }
            avg.lat = lat/counter;
            avg.lng = lng/counter;

            avgLatLng.add(avg);
        }

        return avgLatLng;
    }
}
