package com.email_reply.email_reply_generator.controller;

import com.email_reply.email_reply_generator.dto.EmailReplyRequest;
import com.email_reply.email_reply_generator.dto.EmailReplyResponse;
import com.email_reply.email_reply_generator.service.EmailReplyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class EmailReplyController {

    @Autowired
    private EmailReplyService emailReplyService;

    @GetMapping
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("API is running");
    }

    @PostMapping("/generate-reply")
    public ResponseEntity<EmailReplyResponse> generateReply(@RequestBody EmailReplyRequest request) {
        log.info("Received request to generate reply with tone: {}", request.getTone());
        
        // Create a basic email context from the request
        EmailReplyService.EmailContext context = EmailReplyService.EmailContext.createBasic(
                "Email Subject", // Subject would ideally be parsed from the content
                request.getContent(),
                "Unknown Sender" // Sender would ideally be parsed from the content
        );
        
        String generatedReply = emailReplyService.generateEmailReply(context, request.getTone());
        
        return ResponseEntity.ok(new EmailReplyResponse(generatedReply));
    }
}
