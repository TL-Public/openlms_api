package com.tl.reap_admin_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    JpaConfig.class,
    SecurityConfig.class,
    AppProperties.class
})
public class AppConfig {
    // You can add application-wide beans here if needed
}