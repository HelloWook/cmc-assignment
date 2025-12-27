package com.example.cmc.config;

import com.example.cmc.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class SwaggerAuthInterceptor implements HandlerInterceptor {
    
    private static final String SESSION_USER_KEY = "user";
    private static final String ADMIN_ROLE = "ADMIN";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        
        if (session == null || !isAdmin(session)) {
            response.setStatus(403);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("관리자만 볼 수 있습니다.");
            return false;
        }
        
        return true;
    }

    private boolean isAdmin(HttpSession session) {
        UserResponse user = (UserResponse) session.getAttribute(SESSION_USER_KEY);
        return user != null && ADMIN_ROLE.equals(user.getRole());
    }
}

