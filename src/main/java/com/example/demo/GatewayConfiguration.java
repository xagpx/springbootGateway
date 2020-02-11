package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import com.example.demo.rateLimiter.UriKeyResolver;
@Configuration
public class GatewayConfiguration {
	@Bean
	@LoadBalanced
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
 	@Autowired
 	RedisRateLimiter redisRateLimiter;
	
	@Bean
	public RouteLocator myRoutes(RouteLocatorBuilder builder) {
		//不显示限流head 
		 redisRateLimiter.setIncludeHeaders(false);
		return builder.routes()
				.route(
				p -> p.path("/hello")
				.filters(f -> 
				f.addRequestHeader("Hello", "World")
				 .addRequestParameter("name", "zhangsan")
				 .requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter()))
				)
				.uri("lb://eureka-provider"))
				.build();
	}
	//redis-rate-limiter.replenishRate ： 允许用户每秒处理多少个请求。这是令牌桶被填充的速率。
    //redis-rate-limiter.burstCapacity ： 用户在一秒钟内允许执行的最大请求数。这是令牌桶可以容纳的令牌数量。将此值设置为0将阻塞所有请求
    @Bean
	public RedisRateLimiter redisRateLimiter() {
         return new RedisRateLimiter(1,1);
     }
}