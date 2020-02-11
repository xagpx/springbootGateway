package com.example.demo;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.setResponseStatus;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.HttpStatusHolder;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.alibaba.fastjson.JSONObject;

import reactor.core.publisher.Mono;

@Component
public class RateCheckGatewayFilterFactory extends AbstractGatewayFilterFactory<RateCheckGatewayFilterFactory.Config> implements ApplicationContextAware {
    private static Logger log = LoggerFactory.getLogger(RateCheckGatewayFilterFactory.class);
    private static ApplicationContext applicationContext; 
    private RedisRateLimiter rateLimiter;
    private KeyResolver keyResolver;

    public RateCheckGatewayFilterFactory() {
        super(Config.class);
    }
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        log.info("RateCheckGatewayFilterFactory.setApplicationContext，applicationContext=" + context);
        applicationContext = context;
    }

    @Override
    public GatewayFilter apply(Config config) {
        this.rateLimiter = applicationContext.getBean(RedisRateLimiter.class);
        this.keyResolver = applicationContext.getBean(config.keyResolver, KeyResolver.class);
         return (exchange, chain) -> {
            Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);

            return keyResolver.resolve(exchange).flatMap(key ->
                    // TODO: if key is empty?
                    rateLimiter.isAllowed(route.getId(), key).flatMap(response -> {
                        log.info("response: " + response);
                        // TODO: set some headers for rate, tokens left
                        if (response.isAllowed()) {
                            return chain.filter(exchange);
                        }
                        //超过了限流的response返回值
                        return setRateCheckResponse(exchange);
                    }));
        }; 
    }

    private Mono<Void> setRateCheckResponse(ServerWebExchange exchange) {
        //超过了限流
        ServerHttpResponse response = exchange.getResponse();
        //设置headers
        HttpHeaders httpHeaders = response.getHeaders();
        httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
        httpHeaders.add("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        //设置body
        JSONObject jsonPackage = new JSONObject();
        jsonPackage.put("status",HttpStatus.TOO_MANY_REQUESTS.value());
        jsonPackage.put("message","系统繁忙，请稍后重试");
        DataBuffer bodyDataBuffer = response.bufferFactory().wrap(jsonPackage.toJSONString().getBytes());

        return response.writeWith(Mono.just(bodyDataBuffer));
    }

    public static class Config {
        private String keyResolver;//限流id
        private Boolean denyEmptyKey;

		private String emptyKeyStatus;

		private String routeId;
        private HttpStatus statusCode = HttpStatus.TOO_MANY_REQUESTS;
        public String getKeyResolver() {
            return keyResolver;
        }
        public void setKeyResolver(String keyResolver) {
            this.keyResolver = keyResolver;
        }
        public HttpStatus getStatusCode() {
			return statusCode;
		}

		public Config setStatusCode(HttpStatus statusCode) {
			this.statusCode = statusCode;
			return this;
		}
		public Boolean getDenyEmptyKey() {
			return denyEmptyKey;
		}
		public void setDenyEmptyKey(Boolean denyEmptyKey) {
			this.denyEmptyKey = denyEmptyKey;
		}
		public String getEmptyKeyStatus() {
			return emptyKeyStatus;
		}
		public void setEmptyKeyStatus(String emptyKeyStatus) {
			this.emptyKeyStatus = emptyKeyStatus;
		}
		public String getRouteId() {
			return routeId;
		}
		public void setRouteId(String routeId) {
			this.routeId = routeId;
		}
		
    }
}