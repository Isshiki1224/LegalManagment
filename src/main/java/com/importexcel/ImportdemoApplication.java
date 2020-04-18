package com.importexcel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
@EnableDiscoveryClient
public class ImportdemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImportdemoApplication.class, args);
    }

}
