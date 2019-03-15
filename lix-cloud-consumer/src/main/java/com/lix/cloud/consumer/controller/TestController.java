package com.lix.cloud.consumer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Administrator on 2019/3/8 0008.
 */
@RestController
@RequestMapping("/consumer")
public class TestController {

    @GetMapping("/hello")
    public String hello(@RequestParam String name){
        return "hello,".concat(name);
    }
}
