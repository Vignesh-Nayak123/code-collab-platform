package com.cce.codeeditor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CodeEditorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeEditorApplication.class, args);
    }
}
