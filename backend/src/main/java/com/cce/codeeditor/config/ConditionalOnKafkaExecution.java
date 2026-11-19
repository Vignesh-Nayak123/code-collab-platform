package com.cce.codeeditor.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registers the annotated bean only when code execution is routed through Kafka
 * ({@code app.execution.mode=kafka}, the default). In the other modes
 * ({@code inline}, {@code disabled}) no Kafka producer, consumer, topic or
 * client bean is created, so the app starts and runs without a broker.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnProperty(name = "app.execution.mode", havingValue = "kafka", matchIfMissing = true)
public @interface ConditionalOnKafkaExecution {
}
