package com.example.stravarefactoring.Service;

import com.example.stravarefactoring.Repository.RideBatchRepository;
import com.example.stravarefactoring.TestKakaoApiClient;
import com.example.stravarefactoring.domain.Ride;
import com.google.maps.model.LatLng;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;
import java.util.concurrent.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class ParallelLocationMapper {
    private final Integer THREAD_POOL_SIZE = 10;
    private final Double THREAD_ASSIGNMENT_SIZE = 5.0;
    private final KakaoApiClient kakaoApiClient;
    private Boolean available = true;
    BasicThreadFactory factory = new BasicThreadFactory.Builder().namingPattern("parallelThread-%d").build();
    ExecutorService service = Executors.newFixedThreadPool(THREAD_POOL_SIZE, factory);

    @Autowired
    RideBatchRepository rideBatchRepository;

    @Async("MapperAsyncExecutor")
    public CompletableFuture<HashMap<String, Object>> getLocation(List<Ride> rideList){
        if(!available){
            log.info("LocationMapper is not available");
            HashMap<String, Object> map = new HashMap<>();

            map.put("status", "exception");
            map.put("remain", rideList);
            map.put("result", new HashSet<>());
            return CompletableFuture.completedFuture(map);
        }

        String name = rideList.get(0).getUser().getName();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        log.info("LocationMapper Start. userName : {} data size : {}",name, rideList.size());

        int segmentSize = (int) Math.ceil(rideList.size() / THREAD_ASSIGNMENT_SIZE);

        List<Callable<HashMap<String, Object>>> callables = new ArrayList<>();

        for(int i = 0; i < rideList.size(); i+=segmentSize){
            List<Ride> subList;

            try{
                subList = rideList.subList(i, i + segmentSize);
            } catch (IndexOutOfBoundsException e){
                subList = rideList.subList(i, rideList.size());
            }

            List<Ride> finalSubList = subList;
            Callable<HashMap<String, Object>> callable = () -> parallelProcessing(finalSubList);

            callables.add(callable);
        }

        List<Future<HashMap<String, Object>>> futures;
        try {
            futures = service.invokeAll(callables);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        HashMap<String,Object> map = processingFutures(futures);
        stopWatch.stop();

        if(map.get("status").equals("exception")){
            log.info("LocationMapper exception accrued. due to http 429  userName {}", name);
            available = false;
            return CompletableFuture.completedFuture(map);
        }

        HashSet<String> returnSet = (HashSet<String>) map.get("result");
        log.info("LocationMapper complete. userName : {} processing time : {} ms SetSize : {}",name, stopWatch.getTime(), returnSet.size());
        return CompletableFuture.completedFuture(map);
    }



    private HashMap<String, Object> parallelProcessing(List<Ride> rideList){
        log.info("{} ~ {} ride data assigned in {}.   DataSize : {}   processing start", rideList.get(0).getStart_date_local(), rideList.get(rideList.size()-1).getStart_date_local(), Thread.currentThread().getName(), rideList.size());
        HashMap<String, Object> hashMap = new HashMap<>();

        int position = 0;


        HashSet<String> hashSet = new HashSet<>();

        try {
            for(Ride ride : rideList){
                position = rideList.indexOf(ride);
                String polyline = ride.getSummary_polyline();
                HashSet<String> location;
                if(polyline.equals("")) continue;
                try{
                    location = getAddress(polyline);
                } catch (StringIndexOutOfBoundsException e){
                    continue;
                }
                log.info("rideName : {} complete.   userName : {}", ride.getName(), ride.getUser().getName());
                ride.setMapped(true);
                hashSet.addAll(location);
            }
        } catch (WebClientResponseException e){
            e.printStackTrace();
            if(e.getStatusCode().equals(HttpStatus.TOO_MANY_REQUESTS)) {
                log.info("kakao api request capacity full");

                hashMap.put("result", hashSet);
                hashMap.put("status", "exception");
                hashMap.put("remain", rideList.subList(position, rideList.size()));
                hashMap.put("mapped", rideList.subList(0, position));
                return hashMap;
            }
        }
        hashMap.put("result", hashSet);
        hashMap.put("status", "finish");
        log.info("{} ~ {} ride data processing complete.  {} ", rideList.get(0).getStart_date_local(), rideList.get(rideList.size()-1).getStart_date_local(), Thread.currentThread().getName());

        return hashMap;
    }

    private HashSet<String> getAddress(String polyline){

        HashSet<String> hashSet = new HashSet<>();

        List<LatLng> latLngList = decode(polyline);
        latLngList = calculateAvg(latLngList);

        for(LatLng latLng : latLngList){
            HashMap<String, List<LinkedHashMap>> returnData = kakaoApiClient.api(latLng);
            LinkedHashMap<String, HashMap> document = new LinkedHashMap<>();
            try{
                document = returnData.get("documents").get(0);
            }catch (RuntimeException e){
                continue;
            }

            HashMap<String, String> address = document.get("address");

            String l1 = address.get("region_1depth_name");
            String l2 = address.get("region_2depth_name");
            if(l2.contains(" ")){
                l2 = l2.split(" ")[0];
            }

            hashSet.add(l1+l2);
        }
        return hashSet;
    }



    private List<LatLng> decode(String polyline){
        String newEncodedString = polyline.replace("\\\\", "\\");
        List<LatLng> decoded;
        try{
            decoded = decodeProcess(newEncodedString);
        } catch (StringIndexOutOfBoundsException e){
            throw e;
        }

        return decoded;
    }

    private List<LatLng> decodeProcess(String encodedPath) {

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

    private List<LatLng> calculateAvg(List<LatLng> list){

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


    private HashMap<String, Object> processingFutures(List<Future<HashMap<String, Object>>> futures) {
        HashMap<String, Object> returnMap = new HashMap<>();
        List<Ride> remainRide = new ArrayList<>();
        List<Ride> mappedRide = new ArrayList<>();
        HashSet<String> locations = new HashSet<>();

        for(Future<HashMap<String, Object>> future : futures){
            try {
                HashMap<String, Object> map = future.get();

                if(map.get("status").equals("exception")){
                    remainRide.addAll((Collection<? extends Ride>) map.get("remain"));
                    mappedRide.addAll((Collection<? extends Ride>) map.get("mapped"));
                }

                locations.addAll((Collection<? extends String>) map.get("result"));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        returnMap.put("result", locations);
        if(! remainRide.isEmpty()){
            rideBatchRepository.batchUpdateRides(mappedRide);
            returnMap.put("status", "exception");
            returnMap.put("remain", remainRide);
        } else returnMap.put("status", "finish");

        return returnMap;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public boolean isAvailable(){
        return available;
    }
}
