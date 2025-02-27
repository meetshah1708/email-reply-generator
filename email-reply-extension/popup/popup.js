document.addEventListener('DOMContentLoaded', () => {
    // Load saved settings
    chrome.storage.sync.get(['apiUrl'], (result) => {
      if (result.apiUrl) {
        document.getElementById('api-url').value = result.apiUrl;
      }
    });
    
    // Save settings
    document.getElementById('save-settings').addEventListener('click', () => {
      const apiUrl = document.getElementById('api-url').value;
      chrome.storage.sync.set({ apiUrl }, () => {
        // Show saved message
        const button = document.getElementById('save-settings');
        const originalText = button.textContent;
        button.textContent = 'Saved!';
        setTimeout(() => {
          button.textContent = originalText;
        }, 1500);
      });
    });
  });
  
  // background/background.js
  // Track tab state to monitor Gmail loading
  let gmailTabs = {};
  
  chrome.runtime.onInstalled.addListener(() => {
    console.log('AI Email Reply Generator extension installed');
    
    // Set default settings
    chrome.storage.sync.get(['apiUrl'], (result) => {
      if (!result.apiUrl) {
        chrome.storage.sync.set({ apiUrl: 'http://localhost:8080' });
      }
    });
  });
  
  // Monitor Gmail tab loading
  chrome.tabs.onUpdated.addListener((tabId, changeInfo, tab) => {
    // Check if this is a Gmail tab
    if (tab.url && tab.url.includes('mail.google.com')) {
      // If this is a Gmail tab and it's loaded
      if (changeInfo.status === 'complete') {
        // Inject our content scripts
        chrome.scripting.executeScript({
          target: { tabId: tabId },
          function: () => {
            // Signal to content script to check for compose window
            window.dispatchEvent(new CustomEvent('ai_extension_check_compose'));
          }
        });
      }
    }
  });
  
  // Listen for messages from content script
  chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
    if (request.action === 'getApiUrl') {
      chrome.storage.sync.get(['apiUrl'], (result) => {
        sendResponse({ apiUrl: result.apiUrl || 'http://localhost:8080' });
      });
      return true; // Required for async sendResponse
    }
    
    if (request.action === 'logDebug') {
      console.log('Content Script Debug:', request.message);
      sendResponse({ status: 'logged' });
    }
  });
  
  // content-script/content.js
  // Function to find Gmail's compose window
  function findComposeWindow() {
    // Try multiple selectors to increase robustness
    const selectors = [
      'div[role="dialog"][aria-label*="Compose"]',
      'div[role="dialog"][aria-label*="New Message"]',
      '.aAU', // Compose form container
      '.M9'   // Another compose window identifier
    ];
    
    for (const selector of selectors) {
      const element = document.querySelector(selector);
      if (element) return element;
    }
    
    return null;
  }
  
  // Function to find toolbar in compose window
  function findToolbar(composeWindow) {
    const toolbarSelectors = [
      '.aB.gQ.pE', // Formatting toolbar
      '.oc.gU',    // Action buttons container
      '.aal',      // Another toolbar identifier
      '.adx',      // Alternative toolbar identifier
      'div[role="toolbar"]' // Generic toolbar role
    ];
    
    for (const selector of toolbarSelectors) {
      const toolbar = composeWindow.querySelector(selector);
      if (toolbar) return toolbar;
    }
    
    return null;
  }
  
  // Function to find compose text area
  function findComposeArea(composeWindow) {
    const composeAreaSelectors = [
      'div[contenteditable="true"][role="textbox"][aria-label*="Message Body"]',
      'div[contenteditable="true"][role="textbox"]',
      'div[contenteditable="true"]',
      'div[g_editable="true"]'  // Alternative Gmail editable area
    ];
    
    for (const selector of composeAreaSelectors) {
      const composeArea = composeWindow.querySelector(selector);
      if (composeArea) return composeArea;
    }
    
    return null;
  }
  
  // Function to extract email content being replied to
  function getEmailContent(composeWindow) {
    // Try different approaches to find the quoted content
    // 1. Look for the standard Gmail quote class
    const gmailQuote = document.querySelector('.gmail_quote, .h5');
    if (gmailQuote) return gmailQuote.textContent.trim();
    
    // 2. Try to find the original email in the thread
    const originalEmail = document.querySelector('.adn .a3s.aiL');
    if (originalEmail) return originalEmail.textContent.trim();
    
    // 3. Check for hidden input with original content
    const originalInput = document.querySelector('input[name="original_content"]');
    if (originalInput && originalInput.value) return originalInput.value;
    
    // 4. If in the reply/forward view, look for the quoted text section
    const quotedText = composeWindow.querySelector('.aO7');
    if (quotedText) return quotedText.textContent.trim();
    
    return '';
  }
  
  // Function to inject button into compose window
  function injectReplyButton(composeWindow) {
    if (!composeWindow) return;
    
    // Check if button already exists
    if (composeWindow.querySelector('.ai-reply-button')) return;
    
    const toolbar = findToolbar(composeWindow);
    if (!toolbar) {
      console.error('Could not find toolbar in compose window');
      return;
    }
    
    // Create button with appropriate styling matching Gmail
    const aiButton = document.createElement('div');
    aiButton.className = 'ai-reply-button T-I J-J5-Ji aoO T-I-atl';
    aiButton.setAttribute('role', 'button');
    aiButton.innerHTML = `
      <div class="ai-button-content" style="display: flex; align-items: center;">
        <span class="ai-icon" style="margin-right: 5px;">🤖</span>
        <span>AI Reply</span>
      </div>
    `;
    
    // Add hover effect similar to Gmail buttons
    aiButton.style.cssText = `
      cursor: pointer;
      background-color: #1a73e8;
      color: white;
      border-radius: 4px;
      padding: 0 15px;
      height: 36px;
      display: flex;
      align-items: center;
      margin-right: 10px;
      font-family: 'Google Sans', Roboto, sans-serif;
      font-size: 14px;
      font-weight: 500;
      letter-spacing: 0.25px;
    `;
    
    // Add hover styles
    aiButton.addEventListener('mouseover', () => {
      aiButton.style.backgroundColor = '#1765cc';
      aiButton.style.boxShadow = '0 1px 2px 0 rgba(60,64,67,0.3), 0 1px 3px 1px rgba(60,64,67,0.15)';
    });
    
    aiButton.addEventListener('mouseout', () => {
      aiButton.style.backgroundColor = '#1a73e8';
      aiButton.style.boxShadow = 'none';
    });
    
    // Add click event listener
    aiButton.addEventListener('click', (e) => {
      e.preventDefault();
      e.stopPropagation();
      
      // Get the email content
      const emailBody = getEmailContent(composeWindow);
      
      // Show tone selector
      showToneSelector(composeWindow, emailBody);
    });
    
    // Insert the button at appropriate position
    if (toolbar.firstChild) {
      toolbar.insertBefore(aiButton, toolbar.firstChild);
    } else {
      toolbar.appendChild(aiButton);
    }
  }
  
  // Function to show tone selector
  function showToneSelector(composeWindow, emailBody) {
    // Remove any existing tone selector
    const existingSelector = document.querySelector('.tone-selector-container');
    if (existingSelector) {
      existingSelector.remove();
    }
    
    const tones = ['Professional', 'Friendly', 'Concise', 'Detailed'];
    
    // Create tone selector container styled like Gmail dropdowns
    const selectorContainer = document.createElement('div');
    selectorContainer.className = 'tone-selector-container';
    selectorContainer.style.cssText = `
      position: absolute;
      background: white;
      box-shadow: 0 2px 10px rgba(0,0,0,0.2);
      border-radius: 4px;
      padding: 8px 0;
      z-index: 1001;
      font-family: 'Google Sans', Roboto, sans-serif;
      min-width: 180px;
    `;
    
    // Add heading
    const heading = document.createElement('div');
    heading.textContent = 'Select tone for AI reply:';
    heading.style.cssText = `
      padding: 8px 16px;
      font-size: 14px;
      color: #202124;
      border-bottom: 1px solid #e8eaed;
      margin-bottom: 8px;
    `;
    selectorContainer.appendChild(heading);
    
    // Add tone buttons
    tones.forEach(tone => {
      const button = document.createElement('div');
      button.textContent = tone;
      button.style.cssText = `
        padding: 8px 16px;
        cursor: pointer;
        font-size: 14px;
        color: #202124;
      `;
      
      // Add hover effect
      button.addEventListener('mouseover', () => {
        button.style.backgroundColor = '#f5f5f5';
      });
      
      button.addEventListener('mouseout', () => {
        button.style.backgroundColor = 'transparent';
      });
      
      button.addEventListener('click', () => {
        generateReply(composeWindow, emailBody, tone);
        document.body.removeChild(selectorContainer);
      });
      
      selectorContainer.appendChild(button);
    });
    
    // Position and add to DOM
    document.body.appendChild(selectorContainer);
    
    // Position near the compose window
    const aiButton = composeWindow.querySelector('.ai-reply-button');
    if (aiButton) {
      const rect = aiButton.getBoundingClientRect();
      selectorContainer.style.top = `${rect.bottom + 5}px`;
      selectorContainer.style.left = `${rect.left}px`;
    } else {
      // Fallback positioning
      const rect = composeWindow.getBoundingClientRect();
      selectorContainer.style.top = `${rect.top + 50}px`;
      selectorContainer.style.left = `${rect.left + 50}px`;
    }
    
    // Close when clicking outside
    document.addEventListener('click', function closeSelector(e) {
      if (!selectorContainer.contains(e.target) && 
          !(composeWindow.querySelector('.ai-reply-button') && 
            composeWindow.querySelector('.ai-reply-button').contains(e.target))) {
        if (document.body.contains(selectorContainer)) {
          document.body.removeChild(selectorContainer);
        }
        document.removeEventListener('click', closeSelector);
      }
    });
  }
  
  // Function to generate AI reply
  function generateReply(composeWindow, emailBody, tone) {
    // Create Gmail-style loading indicator
    const loadingOverlay = document.createElement('div');
    loadingOverlay.className = 'ai-loading-overlay';
    loadingOverlay.style.cssText = `
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(255, 255, 255, 0.8);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      z-index: 1000;
      border-radius: 8px;
    `;
    
    // Add spinner
    loadingOverlay.innerHTML = `
      <div class="spinner" style="border: 3px solid #f3f3f3; border-top: 3px solid #3498db; border-radius: 50%; width: 30px; height: 30px; animation: spin 1s linear infinite;"></div>
      <div style="margin-top: 15px; font-family: 'Google Sans', Roboto, sans-serif; font-size: 14px;">Generating AI reply...</div>
    `;
    
    // Add animation style
    const style = document.createElement('style');
    style.textContent = `
      @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
      }
    `;
    document.head.appendChild(style);
    
    // Find the compose area container to position the loading overlay
    const composeAreaContainer = findComposeArea(composeWindow)?.parentElement;
    if (composeAreaContainer) {
      composeAreaContainer.style.position = 'relative';
      composeAreaContainer.appendChild(loadingOverlay);
    } else {
      // Fallback to append to compose window
      composeWindow.appendChild(loadingOverlay);
    }
    
    // Get API URL from storage or use default
    chrome.storage.sync.get(['apiUrl'], (result) => {
      const apiUrl = result.apiUrl || 'http://localhost:8080';
      
      // Call backend API
      fetch(`${apiUrl}/api/generate-reply`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          emailContent: emailBody || 'No email content found',
          tone: tone
        })
      })
      .then(response => {
        if (!response.ok) {
          throw new Error(`HTTP error! Status: ${response.status}`);
        }
        return response.json();
      })
      .then(data => {
        // Insert generated reply into compose area
        const composeArea = findComposeArea(composeWindow);
        if (composeArea) {
          // Insert the reply
          composeArea.innerHTML = data.generatedReply;
          
          // Trigger input event to ensure Gmail registers the change
          composeArea.dispatchEvent(new Event('input', { bubbles: true }));
          
          // Focus on the compose area
          composeArea.focus();
        } else {
          throw new Error('Could not find compose area to insert reply');
        }
      })
      .catch(error => {
        console.error('Error generating reply:', error);
        // Show error in a Gmail-style notification
        showNotification(composeWindow, 'Error generating AI reply. Please try again.', 'error');
      })
      .finally(() => {
        // Remove loading overlay
        if (composeAreaContainer && composeAreaContainer.contains(loadingOverlay)) {
          composeAreaContainer.removeChild(loadingOverlay);
        } else if (composeWindow.contains(loadingOverlay)) {
          composeWindow.removeChild(loadingOverlay);
        }
      });
    });
  }
  
  // Function to show Gmail-style notification
  function showNotification(composeWindow, message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = 'ai-notification';
    
    // Style based on message type
    const backgroundColor = type === 'error' ? '#d93025' : '#1a73e8';
    
    notification.style.cssText = `
      position: absolute;
      bottom: 20px;
      left: 50%;
      transform: translateX(-50%);
      background-color: ${backgroundColor};
      color: white;
      padding: 10px 16px;
      border-radius: 4px;
      font-family: 'Google Sans', Roboto, sans-serif;
      font-size: 14px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.2);
      z-index: 1000;
      animation: slideUpFade 0.3s forwards;
    `;
    
    notification.textContent = message;
    
    // Add animation
    const style = document.createElement('style');
    style.textContent = `
      @keyframes slideUpFade {
        from { opacity: 0; transform: translate(-50%, 20px); }
        to { opacity: 1; transform: translate(-50%, 0); }
      }
      @keyframes fadeOut {
        from { opacity: 1; }
        to { opacity: 0; }
      }
    `;
    document.head.appendChild(style);
    
    // Add to DOM
    document.body.appendChild(notification);
    
    // Remove after 4 seconds
    setTimeout(() => {
      notification.style.animation = 'fadeOut 0.3s forwards';
      setTimeout(() => {
        if (document.body.contains(notification)) {
          document.body.removeChild(notification);
        }
      }, 300);
    }, 4000);
  }
  
  // Debug function to log selector statuses
  function debugSelectors() {
    console.log("Debugging Gmail selectors:");
    
    // Test compose window selectors
    const composeSelectors = [
      'div[role="dialog"][aria-label*="Compose"]',
      'div[role="dialog"][aria-label*="New Message"]',
      '.aAU',
      '.M9'
    ];
    
    console.log("Compose window selectors:");
    composeSelectors.forEach(selector => {
      const element = document.querySelector(selector);
      console.log(`  ${selector}: ${element ? "FOUND" : "NOT FOUND"}`);
    });
    
    // Test toolbar selectors
    const composeWindow = findComposeWindow();
    if (composeWindow) {
      const toolbarSelectors = [
        '.aB.gQ.pE',
        '.oc.gU',
        '.aal',
        '.adx',
        'div[role="toolbar"]'
      ];
      
      console.log("Toolbar selectors:");
      toolbarSelectors.forEach(selector => {
        const element = composeWindow.querySelector(selector);
        console.log(`  ${selector}: ${element ? "FOUND" : "NOT FOUND"}`);
      });
      
      // Test compose area selectors
      const composeAreaSelectors = [
        'div[contenteditable="true"][role="textbox"][aria-label*="Message Body"]',
        'div[contenteditable="true"][role="textbox"]',
        'div[contenteditable="true"]',
        'div[g_editable="true"]'
      ];
      
      console.log("Compose area selectors:");
      composeAreaSelectors.forEach(selector => {
        const element = composeWindow.querySelector(selector);
        console.log(`  ${selector}: ${element ? "FOUND" : "NOT FOUND"}`);
      });
    }
  }
  
  // Add debug button in development mode
  function addDebugButton() {
    // Check if button already exists
    if (document.querySelector('.ai-debug-button')) return;
    
    const debugButton = document.createElement('button');
    debugButton.textContent = 'Debug AI Extension';
    debugButton.className = 'ai-debug-button';
    debugButton.style.cssText = `
      position: fixed;
      bottom: 10px;
      right: 10px;
      z-index: 9999;
      padding: 8px;
      background: #ff5722;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    `;
    
    debugButton.addEventListener('click', () => {
      debugSelectors();
      
      // Send debug message to background script
      chrome.runtime.sendMessage({
        action: 'logDebug',
        message: 'Debug button clicked, selectors analyzed'
      });
    });
    
    document.body.appendChild(debugButton);
  }
  
  // Initialize observers for compose window
  function initializeObservers() {
    // Observer for tracking DOM changes to detect compose window
    const composeDomObserver = new MutationObserver((mutations) => {
      const composeWindow = findComposeWindow();
      if (composeWindow && !composeWindow.querySelector('.ai-reply-button')) {
        injectReplyButton(composeWindow);
      }
    });
    
    // Start observing with optimal configuration
    composeDomObserver.observe(document.body, {
      childList: true,
      subtree: true,
      attributes: true,
      attributeFilter: ['role', 'aria-label']
    });
    
    // Also check immediately in case compose window is already open
    const composeWindow = findComposeWindow();
    if (composeWindow && !composeWindow.querySelector('.ai-reply-button')) {
      injectReplyButton(composeWindow);
    }
    
    // Observer for Gmail's dynamic UI changes
    const gmailViewObserver = new MutationObserver(() => {
      // Check for URL changes indicating navigation to compose
      if (window.location.hash.includes('#compose') || 
          window.location.hash.includes('?compose=') || 
          window.location.hash.includes('#inbox?compose=')) {
        setTimeout(() => {
          const composeWindow = findComposeWindow();
          if (composeWindow && !composeWindow.querySelector('.ai-reply-button')) {
            injectReplyButton(composeWindow);
          }
        }, 500); // Short delay to ensure DOM is ready
      }
    });
    
    // Observe title changes (Gmail changes title when navigation happens)
    const titleElement = document.querySelector('title');
    if (titleElement) {
      gmailViewObserver.observe(titleElement, { 
        subtree: true, 
        characterData: true, 
        childList: true 
      });
    }
    
    // Listen for custom event from background script
    window.addEventListener('ai_extension_check_compose', () => {
      setTimeout(() => {
        const composeWindow = findComposeWindow();
        if (composeWindow && !composeWindow.querySelector('.ai-reply-button')) {
          injectReplyButton(composeWindow);
        }
      }, 1000);
    });
    
    // Also check when hash changes (Gmail uses hash-based routing)
    window.addEventListener('hashchange', () => {
      setTimeout(() => {
        const composeWindow = findComposeWindow();
        if (composeWindow && !composeWindow.querySelector('.ai-reply-button')) {
          injectReplyButton(composeWindow);
        }
      }, 500);
    });
  }
  
  // Main initialization
  function initialize() {
    // Check if we are in development mode (enable for testing)
    const isDevelopmentMode = false;
    if (isDevelopmentMode) {
      addDebugButton();
    }
    
    // Wait for Gmail to fully load
    if (document.readyState === 'complete') {
      initializeObservers();
    } else {
      window.addEventListener('load', initializeObservers);
    }
  }
  
  // Start initialization
  initialize();
  
  // Backend Spring Boot API Implementation 
  // Example implementation for reference:
  
  /*
  @RestController
  @RequestMapping("/api")
  @CrossOrigin(origins = {"chrome-extension://*"})
  public class EmailReplyController {
  
      private final EmailReplyService emailReplyService;
  
      public EmailReplyController(EmailReplyService emailReplyService) {
          this.emailReplyService = emailReplyService;
      }
  
      @PostMapping("/generate-reply")
      public ResponseEntity<Map<String, String>> generateReply(@RequestBody EmailReplyRequest request) {
          String generatedReply = emailReplyService.generateReply(
              request.getEmailContent(), 
              request.getTone()
          );
          
          Map<String, String> response = new HashMap<>();
          response.put("generatedReply", generatedReply);
          
          return ResponseEntity.ok(response);
      }
  }*/