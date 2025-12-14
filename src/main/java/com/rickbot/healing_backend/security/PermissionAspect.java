package com.rickbot.healing_backend.security;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

@Aspect
@Component
public class PermissionAspect {

    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;

    public PermissionAspect(JwtUtil jwtUtil, HttpServletRequest request) {
        this.jwtUtil = jwtUtil;
        this.request = request;
    }

    @Around("@annotation(com.rickbot.healing_backend.security.RequirePermission) || @within(com.rickbot.healing_backend.security.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method m = sig.getMethod();
        RequirePermission ann = m.getAnnotation(RequirePermission.class);
        if (ann == null) ann = pjp.getTarget().getClass().getAnnotation(RequirePermission.class);

        String required = ann.value();

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return ResponseEntity.status(403).body(java.util.Map.of("message", "Forbidden"));
        }
        String token = auth.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(403).body(java.util.Map.of("message", "Forbidden"));
        }

        List<String> perms = jwtUtil.getPermissionsFromToken(token);
        if (perms.contains(required)) {
            return pjp.proceed();
        }

        // Not allowed
        return ResponseEntity.status(403).body(java.util.Map.of("message", "Forbidden"));
    }
}
