package com.cce.codeeditor.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Removes the Sec-WebSocket-Extensions header from WebSocket upgrade requests so
 * Tomcat never negotiates the permessage-deflate extension.
 * <p>
 * Since Tomcat 10.1.x the server installs permessage-deflate by default
 * (Constants.INSTALLED_EXTENSIONS), so any client that offers it (browsers and
 * most WebSocket clients do) gets every outgoing STOMP frame independently
 * deflate-compressed. For the small code-change frames this app broadcasts
 * (hundreds of bytes), that costs far more CPU than it saves and was measured as
 * the dominant broadcast bottleneck (outbound threads pinned in
 * java.util.zip.Deflater under load, with delivery stalls).
 * <p>
 * Declining the extension is standard RFC 6455 behaviour: compression is an
 * optional, negotiated enhancement. Message payloads, ordering and delivery are
 * unchanged; frames are simply sent uncompressed.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebSocketNoCompressionFilter implements Filter {

    private static final String EXTENSIONS_HEADER = "Sec-WebSocket-Extensions";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest req && isWebSocketUpgrade(req)) {
            chain.doFilter(new HttpServletRequestWrapper(req) {
                @Override
                public String getHeader(String name) {
                    if (EXTENSIONS_HEADER.equalsIgnoreCase(name)) {
                        return null;
                    }
                    return super.getHeader(name);
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    if (EXTENSIONS_HEADER.equalsIgnoreCase(name)) {
                        return Collections.emptyEnumeration();
                    }
                    return super.getHeaders(name);
                }

                @Override
                public Enumeration<String> getHeaderNames() {
                    List<String> names = new ArrayList<>();
                    Enumeration<String> all = super.getHeaderNames();
                    while (all.hasMoreElements()) {
                        String name = all.nextElement();
                        if (!EXTENSIONS_HEADER.equalsIgnoreCase(name)) {
                            names.add(name);
                        }
                    }
                    return Collections.enumeration(names);
                }
            }, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    private static boolean isWebSocketUpgrade(HttpServletRequest request) {
        String upgrade = request.getHeader("Upgrade");
        return upgrade != null && "websocket".equalsIgnoreCase(upgrade.trim());
    }
}
