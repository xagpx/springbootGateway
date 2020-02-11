package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private LoadBalancerClient loadBalancerClient;
   
    @Autowired
	RestTemplate restTemplate;
   
   /*
    * eureka-provider 注册服务名
    */
	public String getString(String name) {
		String data = restTemplate.getForObject("http://eureka-provider/hello?name=" + name, String.class);
		return data;
	}
    
    public List<String> getUsers() {
        //选择调用的服务的名称
        //ServiceInstance类封装了服务的基本信息，如 IP，端口等
        ServiceInstance si = this.loadBalancerClient.choose("eureka-provider");
        //拼接访问服务的URL
        StringBuffer sb = new StringBuffer();
        //http://server1:8761/getAllUser
        sb.append("http://").append(si.getHost()).append(":").append(si.getPort()).append("/getAllUser");
        //springMVC RestTemplate
        RestTemplate rt = new RestTemplate();
        ParameterizedTypeReference<List<String>> type = new ParameterizedTypeReference<List<String>>() {};

        //ResponseEntity:封装了返回值信息
        ResponseEntity<List<String>> response = rt.exchange(sb.toString(), HttpMethod.GET, null, type);
        List<String> list = response.getBody();
        return list;
    }
}