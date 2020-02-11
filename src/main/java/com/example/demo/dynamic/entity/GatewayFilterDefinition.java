package com.example.demo.dynamic.entity;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 过滤器模型
 * zhuyu 2019-01-17
 */

public class GatewayFilterDefinition {

    //Filter Name
    private String name;
    //对应的路由规则
    private Map<String, String> args = new LinkedHashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getArgs() {
        return args;
    }

    public void setArgs(Map<String, String> args) {
        this.args = args;
    }
}