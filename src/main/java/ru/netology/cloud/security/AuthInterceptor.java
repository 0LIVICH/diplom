package ru.netology.cloud.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.netology.cloud.domain.User;
import ru.netology.cloud.service.TokenService;

import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    public static final String ATTR_USER = "auth.user";
    private final TokenService tokenService;

    public AuthInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        if (path.endsWith("/login")) {
            return true;
        }
        String token = request.getHeader("auth-token");
        if (token == null || token.isBlank()) {
            response.setStatus(401);
            return false;
        }
        Optional<User> user = tokenService.authenticate(token);
        if (user.isEmpty()) {
            response.setStatus(401);
            return false;
        }
        request.setAttribute(ATTR_USER, user.get());
        return true;
    }
}


