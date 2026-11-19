package com.cce.codeeditor.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Browser origins allowed to call the REST API and open the /ws SockJS endpoint,
 * from {@code app.cors.allowed-origins} (env {@code ALLOWED_ORIGINS}): a
 * comma-separated list of origin patterns, e.g.
 * {@code https://my-app.vercel.app,https://my-app-*-me.vercel.app}.
 * Defaults to {@code *} for local development; set it explicitly when deployed.
 */
@Component
@Slf4j
public class AllowedOrigins {

    private final List<String> patterns;

    public AllowedOrigins(@Value("${app.cors.allowed-origins:*}") String raw) {
        this.patterns = Arrays.stream(raw.split(","))
                .map(String::trim)
                .map(origin -> origin.endsWith("/") ? origin.substring(0, origin.length() - 1) : origin)
                .filter(origin -> !origin.isEmpty())
                .toList();
        if (patterns.isEmpty()) {
            log.warn("app.cors.allowed-origins is empty: all cross-origin browser requests "
                    + "and WebSocket handshakes will be rejected");
        } else if (patterns.contains("*")) {
            log.warn("app.cors.allowed-origins allows any origin ('*'); restrict it for deployed environments");
        } else {
            log.info("Allowed origins: {}", patterns);
        }
    }

    public List<String> patterns() {
        return patterns;
    }

    public String[] asArray() {
        return patterns.toArray(String[]::new);
    }
}
