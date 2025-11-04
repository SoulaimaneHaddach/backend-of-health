package com.rickbot.healing_backend;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Suite
@SuiteDisplayName("Healing Backend Full Test Suite")
@SelectPackages({
    "com.rickbot.healing_backend.controller",
    "com.rickbot.healing_backend.service",
    "com.rickbot.healing_backend.security"
})
@SpringBootTest
@ActiveProfiles("test")
class HealingBackendApplicationTests {
    
    @Test
    void contextLoads() {
    }
}
