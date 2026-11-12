package com.phong.sochitieu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// Tắt user mặc định của Spring Security vì app tự quản lý user bằng JWT
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class SoChiTieuApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoChiTieuApplication.class, args);
    }
}
