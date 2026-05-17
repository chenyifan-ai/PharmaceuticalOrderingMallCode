package com.yao.pharmacymall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 医药商城主启动类
 */
@SpringBootApplication
@MapperScan("com.yao.pharmacymall.mapper")
public class PharmacyMallApplication {

    public static void main(String[] args) {
        SpringApplication.run(PharmacyMallApplication.class, args);
    }
}
