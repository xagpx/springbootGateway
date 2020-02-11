package com.example.demo.controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class UserController {

    @Resource
    private UserService userservice;
    
    @RequestMapping("/get/getAllUsers")
    public List<String> getUsers(){
        return userservice.getUsers();
    }
    @RequestMapping("/hellos")
    public String getString(String name){
        return userservice.getString(name);
    }
    @RequestMapping("/fallback")
    public String fallback(){
        return "请求服务繁忙，请稍后重试";
    }
}