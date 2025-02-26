package com.email_reply.email_reply_generator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GeminiAiService {

    @Value("${gemini.api.key}")
    private String apiKey;
    
    @Value("${gemini.api.url}")
    private String baseUrl;
    
    @Value("${gemini.api.model}")
    private String modelName;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Calls the Gemini API to generate content based on a prompt
     * @param prompt The prompt to send to the model
     * @return The generated text response
     */
    public String generateContent(String prompt) {
        try {
            String url = baseUrl + "/" + modelName + ":generateContent?key=" + apiKey;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Create request body according to Gemini API format
            ObjectNode rootNode = objectMapper.createObjectNode();
            ObjectNode contentsNode = objectMapper.createObjectNode();
            contentsNode.put("role", "user");
            
            // Create parts array with text object
            ArrayNode partsArray = objectMapper.createArrayNode();
            ObjectNode partNode = objectMapper.createObjectNode();
            partNode.put("text", prompt);
            partsArray.add(partNode);
            
            contentsNode.set("parts", partsArray);
            
            ArrayNode contentsArray = objectMapper.createArrayNode();
            contentsArray.add(contentsNode);
            
            rootNode.set("contents", contentsArray);
            rootNode.put("generationConfig", objectMapper.createObjectNode()
                .put("temperature", 0.7)
                .put("topK", 40)
                .put("topP", 0.95)
                .put("maxOutputTokens", 8192));
            
            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(rootNode), headers);
            
            log.debug("Sending request to Gemini API: {}", url);
            String response = restTemplate.postForObject(url, request, String.class);
            log.debug("Received response from Gemini API: {}", response);
            
            // Parse the response
            JsonNode responseJson = objectMapper.readTree(response);
            JsonNode candidates = responseJson.path("candidates");
            
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                
                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText();
                }
            }
            
            throw new RuntimeException("Failed to parse response from Gemini API");
        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            return "Error generating content: " + e.getMessage();
        }
    }
}
