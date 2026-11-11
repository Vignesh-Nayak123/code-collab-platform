package com.cce.codeeditor.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.cce.codeeditor.config.TestConfig;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Import(TestConfig.class)
class CodeEditorIntegrationTest {

    @Test
    void applicationContextLoads() {
        assertTrue(true);
    }
}
