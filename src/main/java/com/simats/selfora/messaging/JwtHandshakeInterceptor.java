package com.simats.selfora.messaging;

import com.simats.selfora.entity.User;
import com.simats.selfora.repository.UserRepository;
import com.simats.selfora.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = null;

        // 1. Check Authorization Bearer header
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // 2. Fallback to query parameter ?token=...
        if (!StringUtils.hasText(token)) {
            if (request instanceof ServletServerHttpRequest servletRequest) {
                token = servletRequest.getServletRequest().getParameter("token");
            } else {
                URI uri = request.getURI();
                String query = uri.getQuery();
                if (query != null) {
                    for (String param : query.split("&")) {
                        String[] pair = param.split("=", 2);
                        if (pair.length == 2 && "token".equals(pair[0])) {
                            token = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                            break;
                        }
                    }
                }
            }
        }

        if (!StringUtils.hasText(token) || !jwtTokenProvider.validateToken(token)) {
            return false;
        }

        try {
            String username = jwtTokenProvider.getUsernameFromJwt(token);
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null || Boolean.FALSE.equals(user.getIsActive())) {
                return false;
            }
            attributes.put("userId", user.getId());
            attributes.put("username", user.getUsername());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // No-op
    }
}

