package com.example.stravarefactoring.Service;


import com.example.stravarefactoring.domain.Ride;
import com.google.maps.model.LatLng;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.concurrent.*;

@Component
@Slf4j
public class ParallelLocationMapper {
    private final Integer THREAD_POOL_SIZE = 10;
    private final Double THREAD_ASSIGNMENT_SIZE = 5.0;

    WebClient webClient = WebClient.builder().build();

    BasicThreadFactory factory = new BasicThreadFactory.Builder().namingPattern("ThreadTest-%d").build();
    ExecutorService service = Executors.newFixedThreadPool(THREAD_POOL_SIZE, factory);

    @Async("MapperAsyncExecutor")
    public CompletableFuture<HashSet<String>> getLocation(List<Ride> rideList){
        String name = rideList.get(0).getUser().getName();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        log.info("LocationMapper Start. userName : {}",name);

        int segmentSize = (int) Math.ceil(rideList.size() / THREAD_ASSIGNMENT_SIZE);

        List<Callable<HashSet<String>>> callables = new ArrayList<>();

        for(int i = 0; i < rideList.size(); i+=segmentSize){
            List<Ride> subList;

            try{
                subList = rideList.subList(i, i + segmentSize);
            } catch (IndexOutOfBoundsException e){
                subList = rideList.subList(i, rideList.size());
            }

            List<Ride> finalSubList = subList;
            Callable<HashSet<String>> callable = () -> parallelProcessing(finalSubList);

            callables.add(callable);
        }

        List<Future<HashSet<String>>> futures;
        try {
            futures = service.invokeAll(callables);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        HashSet<String> returnSet = new HashSet<>();

        for(Future<HashSet<String>> future : futures){
            try {
                returnSet.addAll(future.get());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        stopWatch.stop();

        log.info("LocationMapper complete. userName : {}     processing time : {} ms",name, stopWatch.getTime());
        return CompletableFuture.completedFuture(returnSet);
    }



    private HashSet<String> parallelProcessing(List<Ride> rideList){
        log.info("{} ~ {} ride data assigned in {}.   DataSize : {}   processing start", rideList.get(0).getStart_date_local(), rideList.get(rideList.size()-1).getStart_date_local(), Thread.currentThread().getName(), rideList.size());

        HashSet<String> hashSet = new HashSet<>();

        try {
            for(Ride ride : rideList){
                String polyline = ride.getSummary_polyline();
                HashSet<String> location;
                if(polyline.equals("")) continue;
                try{
                    location = getAddress(polyline);
                } catch (StringIndexOutOfBoundsException e){
                    continue;
                }
                log.info("rideName {} complete. ", ride.getName());
                hashSet.addAll(location);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        log.info("{} ~ {} ride data processing complete.  {} ", rideList.get(0).getStart_date_local(), rideList.get(rideList.size()-1).getStart_date_local(), Thread.currentThread().getName());

        return hashSet;
    }

    private HashSet<String> getAddress(String polyline){

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

}
