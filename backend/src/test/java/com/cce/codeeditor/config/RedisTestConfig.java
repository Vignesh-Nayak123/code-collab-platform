package com.cce.codeeditor.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/** Redis stand-ins so application contexts load without a Redis server. */
@TestConfiguration
public class RedisTestConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return Mockito.mock(RedisConnectionFactory.class);
    }

    @Bean
    @SuppressWarnings("unchecked")
    public RedisTemplate<String, Object> redisTemplate() {
        return Mockito.mock(RedisTemplate.class);
    }

    /**
     * The real container subscribes on startup and fails against the mocked
     * connection factory (null connection). @MockBean replaces the app's own
     * definition; the mock is never auto-started.
     */
    @MockBean
    private RedisMessageListenerContainer redisMessageListenerContainer;
}
