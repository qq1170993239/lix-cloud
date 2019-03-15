package com.lix.cloud.producer.controller;

import com.lix.cloud.producer.client.Hello;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Administrator on 2019/3/8 0008.
 */
@RestController
@RequestMapping("/producer")
public class TestController {

    @Autowired
    private Hello hello;

    @GetMapping("/hello")
    public String hello(@RequestParam String name){
        return hello.hello(name);
    }
}
