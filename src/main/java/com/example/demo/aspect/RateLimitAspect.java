package main.java.com.example.demo.aspect;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
@Slf4j
public class RateLimitAspect {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Around("execution(* com.example.demo.controller.AiController.*(..))")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        // Get the principal from method arguments
        Object[] args = joinPoint.getArgs();
        String username = "anonymous";
        
        for (Object arg : args) {
            if (arg instanceof Principal) {
                username = ((Principal) arg).getName();
                break;
            }
        }

        Bucket bucket = cache.computeIfAbsent(username, k -> createBucket());

        if (bucket.tryConsume(1)) {
            return joinPoint.proceed();
        } else {
            log.warn("Rate limit exceeded for user: {}", username);
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Rate limit exceeded. Please try again later."
            );
        }
    }

    private Bucket createBucket() {
        // 10 requests per minute per user
        Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}