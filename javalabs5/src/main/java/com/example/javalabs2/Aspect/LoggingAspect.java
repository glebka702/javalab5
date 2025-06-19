package com.example.javalabs2.Aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    @Around("execution(* com.example.javalabs2.Controller.*.*(..))")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        logger.info("Entering method: {} with arguments: {}", methodName, Arrays.toString(args));
        try {
            Object result = joinPoint.proceed();
            logger.info("Exiting method: {} with result: {}", methodName, result);
            return result;
        } catch (Throwable t) {
            logger.error("Exception in method: {}: {}", methodName, t.getMessage(), t);
            throw t;
        }
    }
}