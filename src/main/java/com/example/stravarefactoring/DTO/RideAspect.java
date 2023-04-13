package com.example.stravarefactoring.DTO;

import com.example.stravarefactoring.Service.StravaService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;


@Component
@Aspect
@RequiredArgsConstructor
public class RideAspect {
    public final StravaService stravaService;

    @Pointcut("@annotation(com.example.stravarefactoring.Annotation.RideConstructor)")
    public void pointCut(){}

    @After("pointCut()")
    public void afterCreate(JoinPoint joinPoint){
        int rideSeq = stravaService.getRideSeq();

        Ride ride = (Ride) joinPoint.getTarget();

        ride.setRideId(rideSeq);

        rideSeq++;

        stravaService.setRideSeq(rideSeq);
    }

    @After("pointCut()")
    public void tst(JoinPoint joinPoint){
        StravaService service = (StravaService) joinPoint.getTarget();

        String set = service.getString().get();

        System.out.println("before!!!!!!!!!!!!!!!!!!!!!!!");
    }

}