package com.corebanking.payment.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* com.corebanking.payment.controller..*(..)) || execution(* com.corebanking.payment.service..*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        StringBuilder maskedArgs = new StringBuilder();
        for (Object arg : args) {
            if (arg != null) {
                String argString = arg.toString();
                // Mask amount
                argString = argString.replaceAll("(?i)(amount=)[^,\\}\\]]+", "$1***");
                // Mask referenceCode
                argString = argString.replaceAll("(?i)(referenceCode=)[^,\\}\\]]+", "$1***");
                maskedArgs.append(argString).append(", ");
            }
        }

        log.info("[Method: {}] [Request: {}]", methodName, maskedArgs.toString());

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - start;
            
            String maskedResult = result != null ? result.toString() : "null";
            maskedResult = maskedResult.replaceAll("(?i)(errorMessage=)[^,\\}\\]]+", "$1***");
            
            log.info("[Method: {}] [ExecutionTime: {}ms] [Result: {}]", methodName, elapsedTime, maskedResult);
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
