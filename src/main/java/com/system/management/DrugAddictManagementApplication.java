package com.system.management;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableJpaAuditing
@SpringBootApplication
@RequiredArgsConstructor
@EnableTransactionManagement
public class DrugAddictManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(DrugAddictManagementApplication.class, args);
    }
}
