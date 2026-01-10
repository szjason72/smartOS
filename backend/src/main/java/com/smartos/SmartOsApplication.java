package com.smartos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SmartOS 应用主类
 * BC4 Pro 光感智能窗帘控制系统
 */
@SpringBootApplication
@EnableScheduling
public class SmartOsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartOsApplication.class, args);
    }
}
