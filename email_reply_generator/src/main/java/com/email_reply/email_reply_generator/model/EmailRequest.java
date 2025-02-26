package com.email_reply.email_reply_generator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a request to generate an email reply.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    /**
     * The original email content to respond to
     */
    private String content;
    
    /**
     * The subject line of the original email (optional)
     */
    private String subject;
    
    /**
     * The desired tone for the reply (professional, friendly, casual)
     */
    private String tone;
}