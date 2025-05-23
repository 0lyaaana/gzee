package com.gzeeday;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * GzeeDay 애플리케이션 메인 클래스
 * - Spring Boot 애플리케이션의 시작점입니다.
 * - JPA Auditing 기능을 활성화하여 엔티티의 생성/수정 시간을 자동으로 관리합니다.
 */
@SpringBootApplication
@EnableJpaAuditing
public class GzeeDayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GzeeDayApplication.class, args);
    }
} 