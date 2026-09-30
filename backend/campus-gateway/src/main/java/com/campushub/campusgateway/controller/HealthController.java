package com.campushub.campusgateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public Mono<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "campus-gateway");
        response.put("timestamp", LocalDateTime.now().toString());
        return Mono.just(response);
    }

    @GetMapping("/api/health")
    public Mono<Map<String, Object>> apiHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "campus-gateway-api");
        response.put("timestamp", LocalDateTime.now().toString());
        return Mono.just(response);
    }
}
