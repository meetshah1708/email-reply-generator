package com.email_reply.email_reply_generator.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;




@Service
@Slf4j
public class EmailReplyService {

    @Autowired
    private PromptService promptService;

    @Autowired
    private GeminiAiService geminiAiService;

    @Value("${gemini.api.model}")
    private String modelName;

    /**
     * Generates an email reply based on the provided email context and desired tone.
     *
     * @param context The parsed email context
     * @param tone The desired tone for the reply (professional, friendly, casual)
     * @return Generated email reply text
     */
    public String generateEmailReply(EmailContext context, String tone) {
        log.debug("Generating email reply with tone: {}", tone);

        // Build the prompt for the AI model
        String prompt = promptService.buildEmailReplyPrompt(context, tone);

        try {
            // Call the Gemini API via our service
            String result = geminiAiService.generateContent(prompt);

            if (result != null) {
                return result;
            } else {
                return "Sorry, I couldn't generate a reply at this time. Please try again later.";
            }
        } catch (Exception e) {
            log.error("Error generating email reply", e);
            return "An error occurred while generating the email reply. Please try again later.";
        }
    }

    @Data
    public static class EmailContext {
        private String subject;
        private String emailContent;
        private String senderName;
        private String recipientFullName;
        private String recipientTitle;
        private boolean jobApplication;
        private String positionAppliedFor;
        private String companyName;
        private int yearsOfExperience;
        private boolean hasAttachments;

        public static EmailContext createBasic(String subject, String emailContent, String senderName) {
            EmailContext context = new EmailContext();
            context.setSubject(subject);
            context.setEmailContent(emailContent);
            context.setSenderName(senderName);
            context.setJobApplication(false);
            return context;
        }

        public static EmailContext createJobApplication(
                String subject,
                String emailContent,
                String senderName,
                String positionAppliedFor,
                String companyName,
                int yearsOfExperience,
                boolean hasAttachments) {
            EmailContext context = new EmailContext();
            context.setSubject(subject);
            context.setEmailContent(emailContent);
            context.setSenderName(senderName);
            context.setJobApplication(true);
            context.setPositionAppliedFor(positionAppliedFor);
            context.setCompanyName(companyName);
            context.setYearsOfExperience(yearsOfExperience);
            context.setHasAttachments(hasAttachments);
            return context;
        }
    }
}