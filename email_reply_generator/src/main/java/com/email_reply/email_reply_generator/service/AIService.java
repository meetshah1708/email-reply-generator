package com.email_reply.email_reply_generator.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for interacting with the AI model API.
 */
@Service
@Slf4j
public class AIService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;
    
    @Value("${gemini.api.url}")
    private String apiUrl;
    
    @Value("${gemini.api.model}")
    private String model;

    public AIService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Sends a prompt to the AI model and returns the generated text response.
     */
    public String generateText(String prompt) {
        try {
            String fullUrl = String.format("%s/%s:generateContent?key=%s", apiUrl, model, apiKey);
            
            // Create request body
            ObjectNode requestBody = objectMapper.createObjectNode();
            ArrayNode contentsArray = requestBody.putArray("contents");
            
            ObjectNode content = contentsArray.addObject();
            ObjectNode parts = content.putObject("parts");
            parts.put("text", prompt);
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
            
            // Make API call
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, request, String.class);
            return parseResponse(response.getBody());
        } catch (Exception e) {
            log.error("Error calling AI API: {}", e.getMessage(), e);
            return "I apologize, but I was unable to generate a response at this time. Please try again later.";
        }
    }
    
    /**
     * Parses the AI API response to extract the generated text.
     */
    private String parseResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode candidates = root.path("candidates");
            
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                
                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText();
                }
            }
            
            log.error("Unexpected API response structure: {}", jsonResponse);
            return "Sorry, I couldn't generate an appropriate reply.";
        } catch (Exception e) {
            log.error("Error parsing API response: {}", e.getMessage(), e);
            return "Sorry, I couldn't generate an appropriate reply.";
        }
    }
}
