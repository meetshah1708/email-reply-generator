// background.js - Background script for the Email Reply Extension

// Listen for installation or update
chrome.runtime.onInstalled.addListener((details) => {
    console.log('Email Reply Extension installed:', details.reason);
    
    // Set default settings if it's a first install
    if (details.reason === 'install') {
        chrome.storage.sync.set({
            enabled: true,
            templates: [
                {
                    name: 'Quick Reply',
                    content: 'Thank you for your email. I\'ll respond in detail soon.'
                },
                {
                    name: 'Meeting Request',
                    content: 'I\'d be happy to meet. Please suggest a few times that work for you.'
                }
            ]
        }, () => {
            console.log('Default settings saved');
        });
    }
});

// Listen for messages from content script or popup
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
    if (message.action === 'getTemplates') {
        chrome.storage.sync.get('templates', (data) => {
            sendResponse({ templates: data.templates || [] });
        });
        return true; // Required for async response
    }
    
    if (message.action === 'saveTemplate') {
        chrome.storage.sync.get('templates', (data) => {
            const templates = data.templates || [];
            templates.push(message.template);
            chrome.storage.sync.set({ templates }, () => {
                sendResponse({ success: true });
            });
        });
        return true; // Required for async response
    }
});

// Optional: Context menu for quick access
chrome.contextMenus.create({
    id: 'email-reply-menu',
    title: 'Email Reply Templates',
    contexts: ['editable']
});

chrome.contextMenus.onClicked.addListener((info, tab) => {
    if (info.menuItemId === 'email-reply-menu') {
        chrome.tabs.sendMessage(tab.id, { action: 'showTemplates' });
    }
});