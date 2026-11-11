package com.cce.codeeditor.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.cce.codeeditor.config.TestConfig;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Import(TestConfig.class)
class WebSocketIntegrationTest {

    @Test
    void webSocketContextLoads() {
        assertTrue(true);
    }
}
