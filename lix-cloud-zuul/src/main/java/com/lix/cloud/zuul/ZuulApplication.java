package com.lix.cloud.zuul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableZuulProxy
@EnableEurekaClient
@EnableDiscoveryClient
public class LixCloudZuulApplication {

	public static void main(String[] args) {
		SpringApplication.run(LixCloudZuulApplication.class, args);
	}

}
