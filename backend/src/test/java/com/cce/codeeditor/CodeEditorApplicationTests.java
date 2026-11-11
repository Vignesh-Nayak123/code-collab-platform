package com.cce.codeeditor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.cce.codeeditor.config.TestConfig;

import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
@Import(TestConfig.class)
class CodeEditorApplicationTests {

    @Test
    void contextLoads() {
    }
}
