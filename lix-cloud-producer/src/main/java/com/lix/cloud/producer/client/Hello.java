package com.lix.cloud.producer.client;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by Administrator on 2019/3/8 0008.
 */
@FeignClient("service-consumer")
public interface Hello {

    @GetMapping("/consumer/hello")
    String hello(@RequestParam("name") String name);

}
