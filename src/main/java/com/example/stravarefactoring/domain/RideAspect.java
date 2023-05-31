//package com.example.stravarefactoring.DTO;
//
//import com.example.stravarefactoring.Service.StravaService;
//import lombok.RequiredArgsConstructor;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.*;
//import org.springframework.stereotype.Component;
//
//
//@Component
//@Aspect
//@RequiredArgsConstructor
//public class RideAspect {
//    public final StravaService stravaService;
//
//    @Pointcut("@annotation(com.example.stravarefactoring.Annotation.RideConstructor)")
//    public void pointCut(){}
//
//    @Pointcut("execution(* com.fasterxml.jackson.databind.ObjectMapper.readValue(..))")
//    public void objectMapperPointCut(){};
//
//    @After("pointCut()")
//    public void afterCreate(JoinPoint joinPoint){
//        int rideSeq = stravaService.getRideSeq();
//
//        Ride ride = (Ride) joinPoint.getTarget();
//
//        ride.setRideId(rideSeq);
//
//        rideSeq++;
//
//        stravaService.setRideSeq(rideSeq);
//    }
//
//    @Around("objectMapperPointCut()")
//    public void mapperAdvice(ProceedingJoinPoint joinPoint){
//
//        joinPoint.get
//
//
//
//    }
//}