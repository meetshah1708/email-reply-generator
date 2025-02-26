package com.email_reply.email_reply_generator.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class providing helper methods for email processing.
 */
public class EmailUtils {
    
    /**
     * Extracts the email subject from the full email content if present.
     * 
     * @param emailContent The full email content
     * @return The subject line or null if not found
     */
    public static String extractSubject(String emailContent) {
        if (emailContent == null || emailContent.isEmpty()) {
            return null;
        }
        
        Pattern subjectPattern = Pattern.compile("Subject:\\s*([^\\r\\n]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = subjectPattern.matcher(emailContent);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
    
    /**
     * Extracts the sender's name from the email content.
     * 
     * @param emailContent The full email content
     * @return The sender's name or null if not found
     */
    public static String extractSenderName(String emailContent) {
        if (emailContent == null || emailContent.isEmpty()) {
            return null;
        }
        
        // Try to find "From:" header
        Pattern fromPattern = Pattern.compile("From:\\s*\"?([^\"<]+)\"?\\s*(?:<[^>]+>)?", Pattern.CASE_INSENSITIVE);
        Matcher fromMatcher = fromPattern.matcher(emailContent);
        
        if (fromMatcher.find()) {
            return fromMatcher.group(1).trim();
        }
        
        // Try to find signature at the end
        Pattern signaturePattern = Pattern.compile("(?:Regards|Sincerely|Thanks|Thank you|Yours|Cheers|Best),?\\s*[\\r\\n]+\\s*([A-Za-z]+(?:\\s+[A-Za-z]+){0,2})\\s*$", 
                Pattern.CASE_INSENSITIVE);
        Matcher signatureMatcher = signaturePattern.matcher(emailContent);
        
        if (signatureMatcher.find()) {
            return signatureMatcher.group(1).trim();
        }
        
        return null;
    }
    
    /**
     * Determines if the email is likely a job application.
     * 
     * @param emailContent The full email content
     * @return True if the email appears to be a job application
     */
    public static boolean isJobApplication(String emailContent) {
        if (emailContent == null || emailContent.isEmpty()) {
            return false;
        }
        
        String lowerCaseContent = emailContent.toLowerCase();
        
        // Check for common job application phrases
        return lowerCaseContent.contains("apply") && lowerCaseContent.contains("position") ||
               lowerCaseContent.contains("application") && lowerCaseContent.contains("job") ||
               lowerCaseContent.contains("resume") && lowerCaseContent.contains("position") ||
               lowerCaseContent.contains("cv") && lowerCaseContent.contains("vacancy") ||
               (lowerCaseContent.contains("application") && lowerCaseContent.contains("role"));
    }
    
    /**
     * Determines if the email is likely a customer complaint.
     * 
     * @param emailContent The full email content
     * @return True if the email appears to be a customer complaint
     */
    public static boolean isComplaint(String emailContent) {
        if (emailContent == null || emailContent.isEmpty()) {
            return false;
        }
        
        String lowerCaseContent = emailContent.toLowerCase();
        
        // Check for common complaint phrases
        return lowerCaseContent.contains("complaint") ||
               lowerCaseContent.contains("issue") && lowerCaseContent.contains("experiencing") ||
               lowerCaseContent.contains("problem") && lowerCaseContent.contains("service") ||
               lowerCaseContent.contains("disappointed") ||
               lowerCaseContent.contains("unacceptable") ||
               lowerCaseContent.contains("poor service");
    }
    
    /**
     * Returns the appropriate greeting based on the recipient's name and the desired tone.
     * 
     * @param recipientName The recipient's name or null
     * @param tone The desired tone (professional, friendly, casual)
     * @return An appropriate greeting line
     */
    public static String generateGreeting(String recipientName, String tone) {
        if (recipientName == null || recipientName.trim().isEmpty()) {
            switch (tone.toLowerCase()) {
                case "professional":
                    return "Dear Sir/Madam,";
                case "friendly":
                    return "Hello,";
                default:
                    return "Hi there,";
            }
        } else {
            switch (tone.toLowerCase()) {
                case "professional":
                    return "Dear " + recipientName + ",";
                case "friendly":
                    return "Hello " + recipientName + ",";
                default:
                    return "Hi " + recipientName + ",";
            }
        }
    }
    
    /**
     * Returns the appropriate closing based on the desired tone.
     * 
     * @param senderName The sender's name or null
     * @param tone The desired tone (professional, friendly, casual)
     * @return An appropriate closing line
     */
    public static String generateClosing(String senderName, String tone) {
        String closing;
        
        switch (tone.toLowerCase()) {
            case "professional":
                closing = "Sincerely,";
                break;
            case "friendly":
                closing = "Best regards,";
                break;
            default:
                closing = "Thanks,";
                break;
        }
        
        if (senderName != null && !senderName.trim().isEmpty()) {
            return closing + "\n" + senderName;
        } else {
            return closing;
        }
    }
    
    /**
     * Removes quoted text and email headers from an email body.
     * 
     * @param emailContent The full email content
     * @return Cleaned email body
     */
    public static String cleanEmailContent(String emailContent) {
        if (emailContent == null) {
            return "";
        }
        
        // Remove email header lines
        String cleaned = emailContent.replaceAll("(?i)^(?:From|To|Subject|Date|Sent|Cc|Bcc):.*$\\R", "");
        
        // Remove quoted text (lines starting with >)
        cleaned = cleaned.replaceAll("(?m)^>.*$\\R?", "");
        
        // Remove forwarded message markers
        cleaned = cleaned.replaceAll("(?i)----+ ?Forwarded message ?----+.*?\\R", "");
        
        // Remove email signatures
        cleaned = cleaned.replaceAll("(?i)----+\\R[\\s\\S]*", "");
        
        return cleaned.trim();
    }
}
