package com.happinesea.webcrawler.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Configuration
public class PostContentsInterceptor {
	
	
	@Around("execution(* com.example.repository.ContentsPostRepositoryImpl.postContents(..))")
    public Object aroundPostContents(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        log.info(">>> Before postContents: args={}", args);

        try {
            Object result = joinPoint.proceed();
            log.info("<<< After postContents: result={}", result);
            return result;
        } catch (Exception e) {
            log.error("!!! Exception in postContents", e);
            throw e; // 例外をそのまま投げる or ラップして投げる
        }
    }
}
