package com.email_reply.email_reply_generator.service;

import org.springframework.stereotype.Service;

@Service
public class PromptService {

    /**
     * Builds a prompt for generating an email reply based on the context and desired tone.
     *
     * @param context The email context
     * @param tone The desired tone for the reply
     * @return A formatted prompt for the AI model
     */
    public String buildEmailReplyPrompt(EmailReplyService.EmailContext context, String tone) {
        StringBuilder promptBuilder = new StringBuilder();
        
        promptBuilder.append("You are an AI assistant that helps people write professional email replies. ");
        promptBuilder.append("Please generate a reply to the following email ");
        
        // Add tone instructions
        switch (tone.toLowerCase()) {
            case "professional":
                promptBuilder.append("in a formal, professional tone suitable for business communication. ");
                promptBuilder.append("Use proper business etiquette and maintain a respectful, concise style. ");
                break;
            case "friendly":
                promptBuilder.append("in a warm, friendly tone that is still professional but more personable. ");
                promptBuilder.append("Show enthusiasm and warmth while maintaining appropriate boundaries. ");
                break;
            case "casual":
                promptBuilder.append("in a casual, conversational tone. ");
                promptBuilder.append("Be relaxed and informal while still being respectful. ");
                break;
            default:
                promptBuilder.append("in a balanced, professional tone. ");
        }
        
        // Add email context
        promptBuilder.append("\n\nHere is the email to respond to:\n\n");
        promptBuilder.append(context.getEmailContent());
        
        // Additional instructions
        promptBuilder.append("\n\nPlease format your response as a complete email reply ");
        promptBuilder.append("with appropriate greeting and closing. Do not include any explanations ");
        promptBuilder.append("outside of the email reply itself.");
        
        return promptBuilder.toString();
    }
}