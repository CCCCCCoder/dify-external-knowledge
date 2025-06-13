package com.manleytech.web;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Controller("/hello")
public class TestController {

    @Get(produces = MediaType.TEXT_PLAIN,value = "/world")
    public String index() {
        return "Hello World";
    }

    @Get(produces = MediaType.APPLICATION_JSON,value = "/json")
    public Map<String, Object> json() {
        Map<String, Object> map = new HashMap<>();
        map.put("hello", "world");
        return map;
    }
}
