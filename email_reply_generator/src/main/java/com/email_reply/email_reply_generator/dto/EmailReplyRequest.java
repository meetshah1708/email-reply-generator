package com.email_reply.email_reply_generator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailReplyRequest {
    private String content;
    private String tone;
}
