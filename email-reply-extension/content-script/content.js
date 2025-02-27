// content.js - Content script for Email Reply Extension

// Wait for the DOM to be fully loaded
document.addEventListener('DOMContentLoaded', function() {
    console.log('Email Reply Extension content script loaded');
    
    // Initialize the extension functionality
    initEmailReplyExtension();
});

/**
 * Initializes the Email Reply Extension functionality
 */
function initEmailReplyExtension() {
    // Listen for relevant elements in the email page
    setupEmailDetection();
    
    // Set up message listener for communication with background script
    setupMessageListener();
}

/**
 * Sets up detection for email elements on the page
 */
function setupEmailDetection() {
    // This is a basic implementation that would need to be customized
    // based on the specific email service(s) you're targeting (Gmail, Outlook, etc.)
    
    // Example: Monitor for compose windows or reply buttons
    const observer = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
            if (mutation.addedNodes && mutation.addedNodes.length > 0) {
                checkForEmailElements();
            }
        });
    });
    
    // Start observing the document with the configured parameters
    observer.observe(document.body, { childList: true, subtree: true });
    
    // Also check immediately in case elements are already present
    checkForEmailElements();
}

/**
 * Checks for email interface elements and attaches extension functionality
 */
function checkForEmailElements() {
    // Example implementation - detect reply areas or compose windows
    // This would need to be customized for the specific email service
    
    // For Gmail-like interfaces:
    const replyButtons = document.querySelectorAll('[role="button"][aria-label*="Reply"]');
    const composeAreas = document.querySelectorAll('.compose-area, [role="textbox"]');
    
    replyButtons.forEach(button => {
        // Only add listeners to buttons that don't already have our listeners
        if (!button.dataset.emailReplyExtensionInitialized) {
            button.dataset.emailReplyExtensionInitialized = 'true';
            button.addEventListener('click', handleReplyButtonClick);
        }
    });
    
    composeAreas.forEach(area => {
        if (!area.dataset.emailReplyExtensionInitialized) {
            area.dataset.emailReplyExtensionInitialized = 'true';
            injectReplyHelperInterface(area);
        }
    });
}

/**
 * Handles click events on reply buttons
 * @param {Event} event - The click event
 */
function handleReplyButtonClick(event) {
    console.log('Reply button clicked');
    // Wait for the reply area to appear and then enhance it
    setTimeout(() => checkForEmailElements(), 500);
}

/**
 * Injects the reply helper interface near a compose area
 * @param {Element} composeArea - The email compose area element
 */
function injectReplyHelperInterface(composeArea) {
    // Create a button for the email reply functionality
    const helperButton = document.createElement('button');
    helperButton.textContent = 'Generate Reply';
    helperButton.className = 'email-reply-extension-button';
    helperButton.style.cssText = `
        background-color: #4285f4;
        color: white;
        border: none;
        border-radius: 4px;
        padding: 8px 12px;
        margin: 5px;
        cursor: pointer;
    `;
    
    helperButton.addEventListener('click', () => {
        // Get the email content we're replying to
        const emailContent = getEmailContent();
        
        // Request reply suggestions from the extension's background script
        chrome.runtime.sendMessage(
            { action: 'generateReply', emailContent: emailContent },
            (response) => {
                if (response && response.replyText) {
                    insertReplyText(composeArea, response.replyText);
                }
            }
        );
    });
    
    // Insert the button near the compose area
    const parentElement = composeArea.parentElement;
    parentElement.insertBefore(helperButton, composeArea);
}

/**
 * Gets the content of the email being replied to
 * @returns {string} The email content
 */
function getEmailContent() {
    // This is a simplified implementation
    // In a real extension, you would need to extract the actual email content
    // based on the structure of the email service
    
    const emailElements = document.querySelectorAll('.email-content, .message-body');
    let emailContent = '';
    
    emailElements.forEach(element => {
        emailContent += element.textContent + '\n';
    });
    
    return emailContent || 'Unable to extract email content';
}

/**
 * Inserts the generated reply text into the compose area
 * @param {Element} composeArea - The email compose area element
 * @param {string} replyText - The generated reply text
 */
function insertReplyText(composeArea, replyText) {
    // Insert the text into the compose area
    // This would need to be adapted to the specific email service
    
    if (composeArea.isContentEditable) {
        composeArea.textContent = replyText;
    } else {
        composeArea.value = replyText;
    }
    
    // Focus the compose area to allow immediate editing
    composeArea.focus();
}

/**
 * Sets up listener for messages from the extension's background script
 */
function setupMessageListener() {
    chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
        if (message.action === 'insertReply') {
            const activeComposeArea = document.querySelector('[role="textbox"]:focus');
            
            if (activeComposeArea) {
                insertReplyText(activeComposeArea, message.replyText);
                sendResponse({ success: true });
            } else {
                sendResponse({ success: false, error: 'No active compose area found' });
            }
            return true; // Indicate we'll respond asynchronously
        }
    });
}