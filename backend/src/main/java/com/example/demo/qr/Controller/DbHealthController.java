package com.example.demo.qr.Controller;

import java.time.OffsetDateTime;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("supabase")
@RestController
@RequestMapping("/api/health")
public class DbHealthController {

    private final JdbcTemplate jdbcTemplate;

    public DbHealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/db")
    public Map<String, Object> db() {
        OffsetDateTime dbTime = jdbcTemplate.queryForObject("select now()", OffsetDateTime.class);
        return Map.of(
            "status", "ok",
            "database", "supabase-postgres",
            "dbTime", dbTime
        );
    }
}
