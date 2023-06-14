package com.example.stravarefactoring.Service;

import com.example.stravarefactoring.domain.Ride;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.LatLng;
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
        double averageTime = 0L;
        double processingTime = 0L;

        int userId = rideList.get(0).getUser().getId();
        log.info("{} mapper running. userID : {}", Thread.currentThread().getName(), rideList.get(0).getUser().getId());

        HashSet<String> returnSet = new HashSet<>();

        for(Ride ride : rideList){
            String polyline = ride.getSummary_polyline();
            if(polyline.equals("")) continue;
            HashSet<String> location;
            try {
                Long beforeTime = System.nanoTime();
                location = getAddress(polyline);
                Long afterTime = System.nanoTime();
                processingTime = afterTime - beforeTime;
                averageTime += processingTime;

            } catch (RuntimeException e){
                e.printStackTrace();
                continue;
            }

            returnSet.addAll(location);
            log.info("{} end.   processing time : {}", ride.getName(), processingTime);
        }

        log.info("location Mapping Complete.   average processing time per ride : {}", averageTime/rideList.size());
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
            decoded = decodeProcess(newEncodedString);
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


    public List<LatLng> decodeProcess(String encodedPath) {

        int len = encodedPath.length();

        final List<LatLng> path = new ArrayList<>(len / 2);
        int index = 0;
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int result = 1;
            int shift = 0;
            int b;

            try {
                do {
                    b = encodedPath.charAt(index++) - 63 - 1;
                    result += b << shift;
                    shift += 5;
                } while (b >= 0x1f);
                lat += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

                result = 1;
                shift = 0;
                do {
                    b = encodedPath.charAt(index++) - 63 - 1;
                    result += b << shift;
                    shift += 5;
                } while (b >= 0x1f);
                lng += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

                path.add(new LatLng(lat * 1e-5, lng * 1e-5));
            } catch (StringIndexOutOfBoundsException e){
                log.info("String out of index");
            }
        }

        return path;
    }
}
