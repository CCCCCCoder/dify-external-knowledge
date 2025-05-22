package com.manleytech.web;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/hello")
public class TestController {

    @Get(produces = MediaType.TEXT_PLAIN,value = "/world")
    public String index() {
        return "Hello World";
    }
}
