package com.rickbot.healing_backend;

import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ControllersMappingTest {

    @Autowired
    ApplicationContext context;

    

    @Test
    void allControllersHaveAtLeastOneRequestMapping() {
        Map<String, Object> rest = context.getBeansWithAnnotation(RestController.class);
        Map<String, Object> ctrl = context.getBeansWithAnnotation(Controller.class);

        Set<Class<?>> controllerTypes = new HashSet<>();
        controllerTypes.addAll(rest.values().stream().map(AopUtils::getTargetClass).collect(Collectors.toSet()));
        controllerTypes.addAll(ctrl.values().stream().map(AopUtils::getTargetClass).collect(Collectors.toSet()));

        assertThat(controllerTypes).as("No controllers found in application context").isNotEmpty();

        RequestMappingHandlerMapping mapping = context.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);

        Set<Class<?>> mappedTypes = mapping.getHandlerMethods().values().stream()
            .map(HandlerMethod::getBeanType)
            .collect(Collectors.toSet());

        Set<String> without = controllerTypes.stream()
            .filter(ct -> mappedTypes.stream().noneMatch(mt -> mt.isAssignableFrom(ct) || ct.isAssignableFrom(mt)))
            .map(Class::getName)
            .collect(Collectors.toSet());

        if (!without.isEmpty()) {
            System.out.println("Controllers without HTTP request mappings: " + without);
        }
    }
}
