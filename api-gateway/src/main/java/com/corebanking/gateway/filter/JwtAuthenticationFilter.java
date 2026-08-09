package com.corebanking.gateway.filter;

import com.corebanking.gateway.util.JwtUtils;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        super(Config.class);
        this.jwtUtils = jwtUtils;
    }

    public static class Config {
        // Configuration properties can be added here if needed
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return this.onError(exchange.getResponse(), "Missing Authorization Header");
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
                return this.onError(exchange.getResponse(), "Invalid Authorization Header");
            }

            String token = authHeader.substring(7);

            if (!jwtUtils.validateJwtToken(token)) {
                return this.onError(exchange.getResponse(), "Invalid or Expired JWT Token");
            }

            // Extract identity number and attach it to the request header for downstream services
            String identityNumber = jwtUtils.getIdentityNumberFromJwtToken(token);
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-Identity-Number", identityNumber)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    private Mono<Void> onError(ServerHttpResponse response, String err) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }
}
