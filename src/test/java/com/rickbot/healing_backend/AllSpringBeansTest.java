package com.rickbot.healing_backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=update"
})
public class AllSpringBeansTest {

    @Autowired
    ApplicationContext context;

    @Test
    void allDetectedComponentsAreRegisteredAsBeans() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        List<TypeFilter> filters = List.of(
                new AnnotationTypeFilter(Component.class),
                new AnnotationTypeFilter(Service.class),
                new AnnotationTypeFilter(Repository.class),
                new AnnotationTypeFilter(Controller.class),
                new AnnotationTypeFilter(RestController.class)
        );
        filters.forEach(scanner::addIncludeFilter);

        List<String> missing = new ArrayList<>();

        var candidates = scanner.findCandidateComponents("com.rickbot.healing_backend");
        for (var bd : candidates) {
            String className = bd.getBeanClassName();
            if (className == null) continue;
            // Skip test-only configuration classes that are not registered in the main context
            if (className.endsWith("TestConfig") || className.endsWith("Test")) continue;
            Class<?> clazz = Class.forName(className);
            try {
                var beans = context.getBeansOfType(clazz);
                if (beans == null || beans.isEmpty()) {
                    missing.add(className);
                }
            } catch (Exception e) {
                missing.add(className + " (error: " + e.getClass().getSimpleName() + ")");
            }
        }

        assertTrue(missing.isEmpty(), "Missing Spring beans for detected components: " + missing);
    }
}
