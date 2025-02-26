package com.email_reply.email_reply_generator.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for analyzing email content and extracting relevant information.
 */
@Service
@Slf4j
public class EmailAnalyzerService {
    
    /**
     * Analyzes an email to determine its context and metadata.
     */
    public EmailContext analyzeEmail(String content, String subject) {
        EmailContext context = new EmailContext();
        
        // Set raw content and subject
        context.setRawContent(content);
        context.setSubject(subject != null ? subject : extractSubject(content));
        
        // Extract sender information
        extractSenderInfo(content, context);
        
        // Extract recipient information
        extractRecipientInfo(content, context);
        
        // Determine email category/type
        determineEmailType(content, subject, context);
        
        log.debug("Analyzed email - Subject: {}, From: {}, Type: {}", 
                context.getSubject(), context.getSenderName(), context.getEmailType());
        
        return context;
    }
    
    /**
     * Extracts the subject from email content if it contains a subject line.
     */
    private String extractSubject(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        
        Pattern subjectPattern = Pattern.compile("Subject:\\s*([^\\r\\n]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = subjectPattern.matcher(content);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
    
    /**
     * Extracts sender information from email content.
     */
    private void extractSenderInfo(String content, EmailContext context) {
        // First check for signature at the end (most common pattern)
        Pattern signaturePattern = Pattern.compile("(?:[\\r\\n]|^)(?:(?:Regards|Sincerely|Thanks|Thank you|Yours|Cheers|Best|Warm regards|Kind regards|Respectfully),?[\\r\\n]+\\s*)([A-Za-z]+(?:\\s+[A-Za-z]+){0,3})\\s*$", Pattern.CASE_INSENSITIVE);
        Matcher signatureMatcher = signaturePattern.matcher(content);
        
        if (signatureMatcher.find()) {
            context.setSenderName(signatureMatcher.group(1).trim());
            return;
        }
        
        // Try to find "From:" header
        Pattern fromPattern = Pattern.compile("From:\\s*\"?([^\"<]+)\"?\\s*(?:<[^>]+>)?", Pattern.CASE_INSENSITIVE);
        Matcher fromMatcher = fromPattern.matcher(content);
        
        if (fromMatcher.find()) {
            context.setSenderName(fromMatcher.group(1).trim());
            return;
        }
        
        // Check for explicit introduction patterns
        Pattern introPattern = Pattern.compile("(?:My name is|I am|This is)\\s+([A-Za-z]+(?:\\s+[A-Za-z]+){0,2})", Pattern.CASE_INSENSITIVE);
        Matcher introMatcher = introPattern.matcher(content);
        
        if (introMatcher.find()) {
            context.setSenderName(introMatcher.group(1).trim());
        }
    }
    
    /**
     * Extracts recipient information from email content.
     */
    private void extractRecipientInfo(String content, EmailContext context) {
        // Check for "Dear [Name]" or "Hi [Name]" format
        Pattern recipientPattern = Pattern.compile("(?:Dear|Hi|Hello|Hey)\\s+([A-Za-z]+(?:\\s+[A-Za-z]+)?)[,:]", Pattern.CASE_INSENSITIVE);
        Matcher recipientMatcher = recipientPattern.matcher(content);
        
        if (recipientMatcher.find()) {
            context.setRecipientName(recipientMatcher.group(1).trim());
            return;
        }
        
        // Check for "To:" header
        Pattern toPattern = Pattern.compile("To:\\s*\"?([^\"<]+)\"?\\s*(?:<[^>]+>)?", Pattern.CASE_INSENSITIVE);
        Matcher toMatcher = toPattern.matcher(content);
        
        if (toMatcher.find()) {
            context.setRecipientName(toMatcher.group(1).trim());
        }
    }
    
    /**
     * Determines the type/category of email based on content and subject.
     */
    private void determineEmailType(String content, String subject, EmailContext context) {
        String combinedText = (content + " " + (subject != null ? subject : "")).toLowerCase();
        
        // Check for meeting request
        if ((combinedText.contains("schedule") || combinedText.contains("meeting") || 
             combinedText.contains("discuss") || combinedText.contains("availability")) && 
            (combinedText.contains("time") || combinedText.contains("when") || 
             combinedText.contains("week") || combinedText.contains("day"))) {
            context.setEmailType("MEETING_REQUEST");
            return;
        }
        
        // Check for project update request
        if ((combinedText.contains("status") || combinedText.contains("update") || 
             combinedText.contains("progress") || combinedText.contains("timeline")) && 
            (combinedText.contains("project") || combinedText.contains("task") || 
             combinedText.contains("initiative"))) {
            context.setEmailType("PROJECT_UPDATE");
            return;
        }
        
        // Check for customer inquiry
        if ((combinedText.contains("interested in") || combinedText.contains("inquiry") || 
             combinedText.contains("information") || combinedText.contains("details") || 
             combinedText.contains("pricing")) && 
            (combinedText.contains("product") || combinedText.contains("service") || 
             combinedText.contains("package") || combinedText.contains("offering"))) {
            context.setEmailType("CUSTOMER_INQUIRY");
            return;
        }
        
        // Check for customer complaint
        if ((combinedText.contains("complaint") || combinedText.contains("issue") || 
             combinedText.contains("problem") || combinedText.contains("disappointed") || 
             combinedText.contains("unacceptable") || combinedText.contains("outage") || 
             combinedText.contains("disappointed"))) {
            context.setEmailType("CUSTOMER_COMPLAINT");
            return;
        }
        
        // Check for job application
        if ((combinedText.contains("application") || combinedText.contains("apply") || 
             combinedText.contains("resume") || combinedText.contains("cv")) && 
            (combinedText.contains("position") || combinedText.contains("job") || 
             combinedText.contains("role") || combinedText.contains("vacancy"))) {
            context.setEmailType("JOB_APPLICATION");
            return;
        }
        
        // Check for follow-up
        if (combinedText.contains("follow") && combinedText.contains("up") || 
            combinedText.contains("following up") || 
            (combinedText.contains("discuss") && combinedText.contains("from") && 
             (combinedText.contains("meeting") || combinedText.contains("call")))) {
            context.setEmailType("FOLLOW_UP");
            return;
        }
        
        // Check for vendor/order communication
        if ((combinedText.contains("order") || combinedText.contains("delivery") || 
             combinedText.contains("shipment")) && 
            (combinedText.contains("status") || combinedText.contains("update") || 
             combinedText.contains("delay") || combinedText.contains("due"))) {
            context.setEmailType("VENDOR_COMMUNICATION");
            return;
        }
        
        // Default to general correspondence
        context.setEmailType("GENERAL_CORRESPONDENCE");
    }
    
    /**
     * Class to hold extracted email context information.
     */
    public static class EmailContext {
        private String rawContent;
        private String subject;
        private String senderName;
        private String recipientName;
        private String emailType;
        
        // Getters and setters with null safety
        public String getRawContent() {
            return rawContent != null ? rawContent : "";
        }
        
        public void setRawContent(String rawContent) {
            this.rawContent = rawContent;
        }
        
        public String getSubject() {
            return subject != null ? subject : "";
        }
        
        public void setSubject(String subject) {
            this.subject = subject;
        }
        
        public String getSenderName() {
            return senderName != null ? senderName : "";
        }
        
        public void setSenderName(String senderName) {
            this.senderName = senderName;
        }
        
        public String getRecipientName() {
            return recipientName != null ? recipientName : "";
        }
        
        public void setRecipientName(String recipientName) {
            this.recipientName = recipientName;
        }
        
        public String getEmailType() {
            return emailType != null ? emailType : "";
        }
        
        public void setEmailType(String emailType) {
            this.emailType = emailType;
        }
    }
}
