package com.email_reply.email_reply_generator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

/**
 * Configuration class for Gemini API properties.
 * This properly binds the custom properties from application.properties
 */
@ConfigurationProperties(prefix = "gemini.api")
@Primary
@Data
@Validated
public class GeminiApiConfig {
    
    /**
     * API key for Gemini AI service
     */
    private String key;
    
    /**
     * Base URL for the Gemini API
     */
    private String url;
    
    /**
     * Model name to use for generation
     */
    private String model;
}
