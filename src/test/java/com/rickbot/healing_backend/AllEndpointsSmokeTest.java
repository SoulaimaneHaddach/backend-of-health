package com.rickbot.healing_backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
public class AllEndpointsSmokeTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    org.springframework.context.ApplicationContext context;

    @Test
    void exerciseAllHttpMappings_no500s() throws Exception {
        List<String> failures = new ArrayList<>();

        RequestMappingHandlerMapping handlerMapping = context.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);

        for (RequestMappingInfo info : handlerMapping.getHandlerMethods().keySet()) {
            if (info.getPatternsCondition() == null) continue;
            if (info.getPatternsCondition().getPatterns() == null || info.getPatternsCondition().getPatterns().isEmpty()) continue;
            for (String pattern : info.getPatternsCondition().getPatterns()) {
                HttpMethod method = info.getMethodsCondition().getMethods().stream().findFirst()
                        .map(m -> HttpMethod.valueOf(m.name()))
                        .orElse(HttpMethod.GET);

                String uri = pattern.replaceAll("\\{[^}]+\\}", "1");

                MockHttpServletRequestBuilder req = MockMvcRequestBuilders.request(method, uri)
                        .header("Accept", "application/json");

                if (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH) {
                    req.content("{}").contentType("application/json");
                }

                try {
                    MvcResult res = mockMvc.perform(req).andReturn();
                    int status = res.getResponse().getStatus();
                    if (status >= 500) {
                        failures.add(uri + " -> " + method + " returned " + status);
                    }
                } catch (Throwable t) {
                    failures.add(uri + " -> " + method + " exception: " + t.getClass().getSimpleName() + ": " + t.getMessage());
                }
            }
        }

        if (!failures.isEmpty()) {
            System.out.println("Endpoint smoke test failures: " + failures);
        }

        assertThat(failures).as("Endpoints returning 5xx or throwing exceptions").isEmpty();
    }
}
