package com.example.stravarefactoring.Service;

import com.example.stravarefactoring.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
@RequiredArgsConstructor
public class TestAspect {


    @Pointcut("@annotation(com.example.stravarefactoring.Annotation.TestAno)")
    public void pointCut(){}

    private final UserRepository userRepository;

    @After("pointCut()")
    public void after(JoinPoint joinPoint){

        userRepository.deleteAll();

    }
}
