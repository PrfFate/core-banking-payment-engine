package com.corebanking.account.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* com.corebanking.account.controller..*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // Very basic masking logic for demonstration
        StringBuilder maskedArgs = new StringBuilder();
        for (Object arg : args) {
            if (arg != null) {
                String argString = arg.toString();
                argString = argString.replaceAll("(?i)(\"password\"\\s*:\\s*\")[^\"]+(\")", "$1***$2");
                argString = argString.replaceAll("(?i)(password=)[^,\\}\\]]+", "$1***");
                maskedArgs.append(argString).append(", ");
            }
        }

        log.info("[Method: {}] [Request: {}]", methodName, maskedArgs.toString());

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - start;
            log.info("[Method: {}] [ExecutionTime: {}ms] [Status: SUCCESS]", methodName, elapsedTime);
            return result;
        } catch (IllegalArgumentException e) {
            log.warn("[Method: {}] [Status: FAILED] [Reason: {}]", methodName, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[Method: {}] [Status: ERROR] [Reason: {}]", methodName, e.getMessage());
            throw e;
        }
    }
}
