package com.rickbot.healing_backend.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {
    private final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.rickbot.healing_backend.controller.*.*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        
        // Log request (➡️)
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        if (logger.isDebugEnabled()) {
            logger.debug("➡️  Request to {}.{}() - {} {} - params={}",
                className,
                methodName,
                request.getMethod(),
                request.getRequestURI(),
                Arrays.toString(joinPoint.getArgs())
            );
        } else {
            logger.info("➡️  Request to {}.{}() - {} {}",
                className,
                methodName,
                request.getMethod(),
                request.getRequestURI()
            );
        }

        try {
            // Execute the method
            Object result = joinPoint.proceed();
            
            // Log response (✅)
            long executionTime = System.currentTimeMillis() - start;
            if (logger.isDebugEnabled()) {
                logger.debug("✅ Response from {}.{}() - took {}ms - response={}",
                    className,
                    methodName,
                    executionTime,
                    result
                );
            } else {
                logger.info("✅ Response from {}.{}() - took {}ms",
                    className,
                    methodName,
                    executionTime
                );
            }
            
            return result;
        } catch (Exception e) {
            // Log error (❌)
            logger.error("❌ Exception in {}.{}() - {}", 
                className, 
                methodName,
                e.getMessage(),
                e
            );
            throw e;
        }
    }
}