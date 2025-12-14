package com.rickbot.healing_backend.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class AdminStore {
    private final Map<Long, AdminUser> store = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public AdminUser create(String username, String displayName, List<String> roles, List<String> perms) {
        Long id = idGen.getAndIncrement();
        AdminUser u = new AdminUser(id, username, displayName, roles == null ? new ArrayList<>() : new ArrayList<>(roles), perms == null ? new ArrayList<>() : new ArrayList<>(perms));
        store.put(id, u);
        return u;
    }

    public List<AdminUser> list() {
        return store.values().stream().collect(Collectors.toList());
    }

    public AdminUser findById(Long id) { return store.get(id); }

    public AdminUser findByUsername(String username) {
        return store.values().stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst().orElse(null);
    }

    public boolean updatePermissions(Long id, List<String> perms) {
        AdminUser u = store.get(id);
        if (u == null) return false;
        u.setPerms(perms == null ? new ArrayList<>() : new ArrayList<>(perms));
        return true;
    }

    public long count() { return store.size(); }

    public void clear() { store.clear(); }
}
