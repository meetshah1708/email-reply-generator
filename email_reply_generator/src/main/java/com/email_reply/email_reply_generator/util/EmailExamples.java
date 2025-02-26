package com.email_reply.email_reply_generator.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class containing example email exchanges for various scenarios.
 * These examples can be used for training, testing, or providing context to the AI model.
 */
public class EmailExamples {
    
    /**
     * Represents an example email exchange with original email and appropriate reply
     */
    public static class EmailExchange {
        private final String scenario;
        private final String subject;
        private final String originalEmail;
        private final String appropriateReply;
        private final String tone;
        
        public EmailExchange(String scenario, String subject, String originalEmail, 
                             String appropriateReply, String tone) {
            this.scenario = scenario;
            this.subject = subject;
            this.originalEmail = originalEmail;
            this.appropriateReply = appropriateReply;
            this.tone = tone;
        }
        
        public String getScenario() { return scenario; }
        public String getSubject() { return subject; }
        public String getOriginalEmail() { return originalEmail; }
        public String getAppropriateReply() { return appropriateReply; }
        public String getTone() { return tone; }
    }
    
    /**
     * Returns a list of example email exchanges across different scenarios
     */
    public static List<EmailExchange> getExampleExchanges() {
        List<EmailExchange> examples = new ArrayList<>();
        
        // Example 1: Meeting Request
        examples.add(new EmailExchange(
            "Meeting Request",
            "Request for Team Meeting",
            "Hi Sarah,\n\nCan we schedule a meeting to discuss the upcoming product launch? I'm available any time this week.\n\nThanks,\nJohn",
            "Dear John,\n\nThank you for reaching out. I'd be happy to meet regarding the product launch. Would Wednesday at 2:00 PM work for you? We can use Conference Room A or set up a virtual meeting.\n\nBest regards,\nSarah",
            "professional"
        ));
        
        // Example 2: Project Update
        examples.add(new EmailExchange(
            "Project Update",
            "Project Delta Status",
            "Hello Team,\n\nPlease provide an update on Project Delta's timeline and current status.\n\nRegards,\nMike",
            "Hi Mike,\n\nHere's the current status of Project Delta:\n- Phase 1: Completed (on schedule)\n- Phase 2: 75% complete, expected completion by Friday\n- Phase 3: Starting next week\n\nAll milestones are currently on track. Would you like me to send the detailed progress report?\n\nBest,\nTeam Lead",
            "professional"
        ));
        
        // Example 3: Customer Inquiry
        examples.add(new EmailExchange(
            "Customer Inquiry",
            "Product Information Request",
            "Hello,\n\nI'm interested in your Premium Package. Could you please send me pricing details?\n\nThanks,\nDavid",
            "Dear David,\n\nThank you for your interest in our Premium Package. I'm pleased to provide you with the following information:\n- Monthly subscription: $99\n- Annual subscription: $999 (saving 16%)\n- Enterprise solutions: Custom pricing\n\nWould you like to schedule a demo to learn more about the features?\n\nBest regards,\nSales Team",
            "professional"
        ));
        
        // Example 4: Formal Apology
        examples.add(new EmailExchange(
            "Formal Apology",
            "Service Interruption Complaint",
            "Dear Support,\n\nI experienced a 2-hour service outage yesterday. This is unacceptable for a premium customer.\n\nRegards,\nJane Smith",
            "Dear Ms. Smith,\n\nI sincerely apologize for the inconvenience caused by yesterday's service interruption. We have identified and resolved the underlying issue. As compensation, we've added a one-month service credit to your account.\n\nWe value your business and are implementing measures to prevent similar incidents.\n\nSincerely,\nCustomer Support Manager",
            "professional"
        ));
        
        // Example 5: Casual Internal Communication
        examples.add(new EmailExchange(
            "Casual Internal Communication",
            "Office Party",
            "Hey everyone,\n\nAnyone up for organizing the holiday party this year?\n\nCheers,\nTom",
            "Hi Tom,\n\nI'd be happy to help organize! Let's form a small committee. I've already got some great venue ideas.\n\nWho else wants to join in? We can meet tomorrow during lunch to brainstorm.\n\nBest,\nLisa",
            "casual"
        ));
        
        // Example 6: Follow-up Email
        examples.add(new EmailExchange(
            "Follow-up Email",
            "Following up on our meeting",
            "Hi Rebecca,\n\nJust following up on our discussion from Monday. Have you had a chance to review the proposal?\n\nBest,\nMark",
            "Hi Mark,\n\nThanks for following up. I've reviewed the proposal and have a few suggestions for modifications. Would you be available for a quick call tomorrow to discuss them?\n\nRegards,\nRebecca",
            "friendly"
        ));
        
        // Example 7: Job Application Response
        examples.add(new EmailExchange(
            "Job Application Response",
            "Application for Marketing Manager Position",
            "Dear HR Team,\n\nI'm writing to apply for the Marketing Manager position advertised on your website.\n[Resume attached]\n\nBest regards,\nAlex Thompson",
            "Dear Mr. Thompson,\n\nThank you for your interest in the Marketing Manager position at our company. We have received your application and will review it carefully.\n\nIf your qualifications match our requirements, we will contact you within the next two weeks to schedule an interview.\n\nBest regards,\nHR Department",
            "professional"
        ));
        
        // Example 8: Vendor Communication
        examples.add(new EmailExchange(
            "Vendor Communication",
            "Supply Order Delay",
            "Hello,\n\nOur order #12345 was due last week. Could you provide an update?\n\nThanks,\nProcurement Team",
            "Dear Valued Customer,\n\nWe apologize for the delay with order #12345. Due to unexpected shipping delays, your order will arrive on Friday. We've expedited the shipping at no extra cost.\n\nAs a gesture of goodwill, we're offering a 10% discount on your next order.\n\nBest regards,\nVendor Support",
            "professional"
        ));
        
        return examples;
    }
    
    /**
     * Returns examples grouped by email scenario type
     */
    public static Map<String, List<EmailExchange>> getExamplesByScenario() {
        Map<String, List<EmailExchange>> groupedExamples = new HashMap<>();
        
        for (EmailExchange example : getExampleExchanges()) {
            if (!groupedExamples.containsKey(example.getScenario())) {
                groupedExamples.put(example.getScenario(), new ArrayList<>());
            }
            groupedExamples.get(example.getScenario()).add(example);
        }
        
        return groupedExamples;
    }
    
    /**
     * Returns examples grouped by tone
     */
    public static Map<String, List<EmailExchange>> getExamplesByTone() {
        Map<String, List<EmailExchange>> groupedExamples = new HashMap<>();
        
        for (EmailExchange example : getExampleExchanges()) {
            if (!groupedExamples.containsKey(example.getTone())) {
                groupedExamples.put(example.getTone(), new ArrayList<>());
            }
            groupedExamples.get(example.getTone()).add(example);
        }
        
        return groupedExamples;
    }
    
    /**
     * Utility method to get an example by scenario name
     */
    public static EmailExchange getExampleByScenario(String scenarioName) {
        for (EmailExchange example : getExampleExchanges()) {
            if (example.getScenario().equalsIgnoreCase(scenarioName)) {
                return example;
            }
        }
        return null;
    }
}
