//package com.example.stravarefactoring.aspect;
//
//import com.example.stravarefactoring.exception.MapperRunningException;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.aspectj.lang.annotation.Pointcut;
//import org.springframework.stereotype.Component;
//
//import java.util.HashMap;
//
//@Component
//@Aspect
//public class MapperAspect {
//
//    HashMap<Integer, Boolean> map = new HashMap<>();
//
//    @Pointcut("@annotation(com.example.stravarefactoring.Annotation.Mapper)")
//    public void pointCut(){}
//
//
//    @Before("pointCut()")
//    public void isRunning(JoinPoint joinPoint){
//        joinPoint.getArgs().
//
//        if(!map.containsKey(userId)){
//            map.put(userId, true);
//            return;
//        }
//        if(map.get(userId)){
//            throw new MapperRunningException("mapper is running    userId : " + userId);
//        }
//    }
//}
