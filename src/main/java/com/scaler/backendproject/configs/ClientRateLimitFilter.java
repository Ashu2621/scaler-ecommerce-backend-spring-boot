package com.scaler.backendproject.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientRateLimitFilter extends OncePerRequestFilter {
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final int limit;
    private final long windowMillis;
    private final Clock clock;

    public ClientRateLimitFilter(int limit, long windowSeconds) {
        this(limit, windowSeconds, Clock.systemUTC());
    }

    ClientRateLimitFilter(int limit, long windowSeconds, Clock clock) {
        this.limit = limit;
        this.windowMillis = windowSeconds * 1000;
        this.clock = clock;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long now = clock.millis();
        String key = clientKey(request);
        Window window = windows.compute(key, (ignored, current) ->
                current == null || now - current.startedAt() >= windowMillis
                        ? new Window(now, new AtomicInteger(1))
                        : increment(current));
        if (window.count().get() > limit) {
            long retryAfter = Math.max(1, (windowMillis - (now - window.startedAt())) / 1000);
            response.setStatus(429);
            response.setHeader("Retry-After", Long.toString(retryAfter));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"code\":\"RATE_LIMITED\",\"message\":\"Too many requests\"}");
            return;
        }
        filterChain.doFilter(request, response);
        if (windows.size() > 10_000) {
            windows.entrySet().removeIf(entry -> now - entry.getValue().startedAt() >= windowMillis);
        }
    }

    private Window increment(Window current) {
        current.count().incrementAndGet();
        return current;
    }

    private String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded == null || forwarded.isBlank()
                ? request.getRemoteAddr()
                : forwarded.split(",", 2)[0].trim();
    }

    private record Window(long startedAt, AtomicInteger count) {
    }
}
