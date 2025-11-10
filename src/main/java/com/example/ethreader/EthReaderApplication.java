package com.example.ethreader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EthReaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(EthReaderApplication.class, args);
    }

}
