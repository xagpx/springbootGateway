package com.example.demo.rateLimiter;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfiguration {

	/*
	 * Hostname进行限流
	 */
	@Bean
    public HostAddrKeyResolver hostAddrKeyResolver() {
        return new HostAddrKeyResolver();
    }
	/*
	 * uri去限流
	 */
 	@Bean
//	@Bean(name="UriKeyResolver")
	@Primary
    public UriKeyResolver uriKeyResolver() {
        return new UriKeyResolver();
    }
	
	@Bean
    KeyResolver userKeyResolver() {
        //按用户限流
        return exchange -> Mono.just(exchange.getRequest().getQueryParams().getFirst("user"));
    }
}
