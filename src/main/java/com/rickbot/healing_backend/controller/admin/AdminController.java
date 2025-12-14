package com.rickbot.healing_backend.controller.admin;

import com.rickbot.healing_backend.security.RequirePermission;
import com.rickbot.healing_backend.security.JwtUtil;
import com.rickbot.healing_backend.service.AdminStore;
import com.rickbot.healing_backend.service.AdminUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/admins")
public class AdminController {

    private final AdminStore adminStore;
    private final JwtUtil jwtUtil;

    public AdminController(AdminStore adminStore, JwtUtil jwtUtil) {
        this.adminStore = adminStore;
        this.jwtUtil = jwtUtil;
    }

    // Bootstrap initial admin only when none exists
    @PostMapping("/bootstrap")
    public ResponseEntity<?> bootstrap(@RequestBody CreateAdminRequest req) {
        if (adminStore.count() > 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Bootstrap not allowed after initial admin created"));
        }
        AdminUser u = adminStore.create(req.username, req.displayName, req.roles, req.perms);
        String token = jwtUtil.generateTokenWithPermissions(u.getUsername(), u.getRoles(), u.getPerms());
        return ResponseEntity.ok(Map.of("admin", u, "token", token));
    }

    @RequirePermission("admins:view")
    @GetMapping("")
    public ResponseEntity<?> list() {
        List<AdminUser> all = adminStore.list();
        return ResponseEntity.ok(all);
    }

    @RequirePermission("admins:create")
    @PostMapping("")
    public ResponseEntity<?> create(@RequestBody CreateAdminRequest req) {
        AdminUser existing = adminStore.findByUsername(req.username);
        if (existing != null) {
            return ResponseEntity.badRequest().body(Map.of("message", "username_exists"));
        }
        AdminUser u = adminStore.create(req.username, req.displayName, req.roles, req.perms);
        String token = jwtUtil.generateTokenWithPermissions(u.getUsername(), u.getRoles(), u.getPerms());
        return ResponseEntity.ok(Map.of("admin", u, "token", token));
    }

    @RequirePermission("admins:update")
    @PutMapping("/{id}/permissions")
    public ResponseEntity<?> updatePerms(@PathVariable("id") Long id, @RequestBody UpdatePermsRequest req) {
        boolean ok = adminStore.updatePermissions(id, req.perms);
        if (!ok) return ResponseEntity.notFound().build();
        AdminUser u = adminStore.findById(id);
        return ResponseEntity.ok(u);
    }

    static class CreateAdminRequest {
        public String username;
        public String displayName;
        public List<String> roles;
        public List<String> perms;
    }

    static class UpdatePermsRequest { public List<String> perms; }
}
