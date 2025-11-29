package com.marketplace.catalog.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.logging.Level;
import java.util.logging.Logger;

@Aspect
public class LoggingAspect {

    private static final Logger log = Logger.getLogger(LoggingAspect.class.getName());

    /**
     * Логируем ВСЕ публичные методы сервисов и репозиториев.
     */
    @Around("execution(public * com.marketplace.catalog.service..*(..)) || " +
            "execution(public * com.marketplace.catalog.repository..*(..))")
    public Object logExecution(ProceedingJoinPoint pjp) throws Throwable {
        long startNanos = System.nanoTime();
        try {
            return pjp.proceed();
        } finally {
            long durationNanos = System.nanoTime() - startNanos;
            long millis = durationNanos / 1_000_000;

            String className = pjp.getSignature().getDeclaringTypeName();
            String methodName = pjp.getSignature().getName();

            log.log(Level.INFO,
                    () -> String.format("[LOG] %s.%s(..) took %d ms",
                            className, methodName, millis));
        }
    }
}
