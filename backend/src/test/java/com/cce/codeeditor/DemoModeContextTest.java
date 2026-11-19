package com.cce.codeeditor;

import com.cce.codeeditor.config.RedisTestConfig;
import com.cce.codeeditor.execution.DisabledExecutionDispatcher;
import com.cce.codeeditor.execution.ExecutionDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/** The free-tier demo mode must start with no Kafka beans at all and never run code. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "app.execution.mode=disabled")
@ActiveProfiles("test")
@Import(RedisTestConfig.class)
class DemoModeContextTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void startsWithoutKafkaAndWithExecutionDisabled() {
        assertThat(context.getBeanNamesForType(KafkaTemplate.class)).isEmpty();
        assertThat(context.containsBean("executionResultConsumer")).isFalse();
        assertThat(context.containsBean("executionRequestProducer")).isFalse();
        assertThat(context.getBean(ExecutionDispatcher.class)).isInstanceOf(DisabledExecutionDispatcher.class);
    }
}
