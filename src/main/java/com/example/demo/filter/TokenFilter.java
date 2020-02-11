package com.example.demo.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
/**
 * 全局过滤器
 * @author zgh
 *
 */
@Service
public class TokenFilter implements GlobalFilter, Ordered {
	 private static final String REQUEST_TIME_BEGIN = "requestTimeBegin";
    Logger logger=LoggerFactory.getLogger( TokenFilter.class );
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    	exchange.getAttributes().put(REQUEST_TIME_BEGIN, System.currentTimeMillis());
    	
    	// 请求对象
        ServerHttpRequest request = exchange.getRequest();
        // 响应对象
        ServerHttpResponse response = exchange.getResponse();
    	
    	String token = exchange.getRequest().getQueryParams().getFirst("token");

    	/*//不合法
        ServerHttpResponse response = exchange.getResponse();
        //设置headers
        HttpHeaders httpHeaders = response.getHeaders();
        httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
        httpHeaders.add("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        //设置body
        JsonPackage jsonPackage = new JsonPackage();
        jsonPackage.setStatus(110);
        jsonPackage.setMessage("未登录或登录超时");
        DataBuffer bodyDataBuffer = response.bufferFactory().wrap(jsonPackage.toJSONString().getBytes());

        return response.writeWith(Mono.just(bodyDataBuffer));*/
    	
    	
  //        if (token == null || token.isEmpty()) {
//            logger.info( "token is empty..." );
//            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//            return exchange.getResponse().setComplete();
//        }
       // return chain.filter(exchange);
    	
    	 // 获取请求地址
        String beforePath = request.getPath().pathWithinApplication().value();
        // 获取响应状态码
        HttpStatus beforeStatusCode = response.getStatusCode();
        System.out.println("响应码：" + beforeStatusCode + "，请求路径：" + beforePath);
        // 请求前
        System.out.println("filter -> before");
        // 如果不为空，就通过
        ServerHttpRequest req = exchange.getRequest().mutate().header("token", "gateway").build();
        return chain.filter(exchange.mutate().request(req.mutate().build()).build()
        		).then(Mono.fromRunnable(() -> {
        	 Long startTime = exchange.getAttribute(REQUEST_TIME_BEGIN);
            // 获取请求地址
            String afterPath = request.getPath().pathWithinApplication().value();
            // 获取响应状态码
            HttpStatus afterStatusCode = response.getStatusCode();
            System.out.println("响应码：" + afterStatusCode + "，请求路径：" + afterPath);
            // 响应后
            System.out.println("filter -> after");
            if (startTime != null) {
            	logger.info(exchange.getRequest().getURI().getRawPath() + ": " + (System.currentTimeMillis() - startTime) + "ms");
            }
        }));
    }

    public int getOrder() {
        return 0;
    }
}