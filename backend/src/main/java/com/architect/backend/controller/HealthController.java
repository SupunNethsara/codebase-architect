package com.architect.backend.controller;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @RestController  = @Controller + @ResponseBody.
 * Return karana object eka auto-magically JSON බවට convert wenawa (Jackson).
 * Spring Boot start wenakota package eka scan karala me class eka bean ekak widiyata register karanawa.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "codebase-architect-backend",
                "time", Instant.now().toString());
    }
}
