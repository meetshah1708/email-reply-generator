package com.email_reply.email_reply_generator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the generated email reply response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailResponse {
    /**
     * The generated reply content
     */
    private String reply;
    
    /**
     * The reply subject line
     */
    private String subject;
}
