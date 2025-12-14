package com.rickbot.healing_backend.seeder;

import com.rickbot.healing_backend.dto.AdminUserRequest;
import com.rickbot.healing_backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final AdminService adminService;

    @Value("${app.admin.email:admin@hospital.com}")
    private String adminEmail;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.admin.name:Administrator}")
    private String adminName;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var exist = adminService.findEntityByEmail(adminEmail);
        if (exist.isEmpty()) {
            AdminUserRequest r = new AdminUserRequest();
            r.setEmail(adminEmail);
            r.setPassword(adminPassword);
            r.setName(adminName);
            r.setRoles(Set.of("ROLE_ADMIN"));
            adminService.create(r);
        }
    }
}
