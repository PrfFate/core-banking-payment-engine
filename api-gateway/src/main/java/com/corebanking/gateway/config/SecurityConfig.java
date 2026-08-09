package com.corebanking.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            // Biz Gateway filter'imiz ile auth yapiyoruz, 
            // o yuzden spring security tarafinda butun endpointleri saliyoruz
            .authorizeExchange(exchanges -> exchanges
                .anyExchange().permitAll()
            );
        return http.build();
    }
}
