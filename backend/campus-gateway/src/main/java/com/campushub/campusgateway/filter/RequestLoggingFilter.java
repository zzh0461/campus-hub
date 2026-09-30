package com.campushub.campusgateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 请求日志过滤器
 */
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        String remoteAddress = request.getRemoteAddress() != null ? 
            request.getRemoteAddress().getHostString() : "unknown";
        
        logger.info("Gateway Request: {} {} from {}", method, path, remoteAddress);
        
        long startTime = System.currentTimeMillis();
        
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            int statusCode = exchange.getResponse().getStatusCode() != null ? 
                exchange.getResponse().getStatusCode().value() : 0;
            
            logger.info("Gateway Response: {} {} - Status: {} - Duration: {}ms", 
                method, path, statusCode, duration);
        }));
    }

    @Override
    public int getOrder() {
        // 高优先级，最先执行
        return -100;
    }
}
