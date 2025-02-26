import React, { useState } from 'react';
import axios from 'axios';
import './EmailReplyGenerator.css';

const EmailReplyGenerator = () => {
  const [content, setContent] = useState('');
  const [tone, setTone] = useState('casual');
  const [reply, setReply] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [connectionStatus, setConnectionStatus] = useState('unknown');

  // Check connection to backend on component mount
  // React.useEffect(() => {
  //   checkBackendConnection();
  // }, []);

  const checkBackendConnection = async () => {
    try {
      // Using a HEAD request to check if server is reachable
      await axios.head('http://localhost:8080/api');
      // setConnectionStatus('connected');
    } catch (error) {
      if (error.code === 'ERR_NETWORK') {
        setConnectionStatus('disconnected');
      } else {
        // If we get any response, the server is running
        setConnectionStatus('connected');
      }
    }
  };

  const generateReply = async () => {
    if (!content.trim()) {
      setError('Please enter email content');
      return;
    }

    setLoading(true);
    setError('');
    
    try {
      const response = await axios.post('http://localhost:8080/api/generate-reply', { content, tone });
      setReply(response.data.reply);
      setError('');
      setConnectionStatus('connected');
    } catch (error) {
      console.error("Error generating reply", error);
      if (error.code === 'ERR_NETWORK') {
        setError('Cannot connect to server. Please check if the backend is running.');
        setConnectionStatus('disconnected');
      } else {
        setError(`Error: ${error.response?.data?.message || error.message}`);
      }
    } finally {
      setLoading(false);
    }
  };

  const copyToClipboard = () => {
    navigator.clipboard.writeText(reply);
    alert('Reply copied to clipboard!');
  };

  return (
    <div className="modern-container">
      <div className="modern-card">
        <div className="modern-header">
          <h2>AI Email Reply Generator</h2>
          {/* <div className={`connection-status ${connectionStatus}`}>
            Backend: {connectionStatus === 'connected' ? 'Connected' : 
                      connectionStatus === 'disconnected' ? 'Disconnected' : 'Checking...'}
          </div> */}
        </div>
        <div className="modern-form">
          <textarea 
            rows="6" 
            className="modern-textarea" 
            placeholder="Paste email content here" 
            value={content} 
            onChange={(e) => setContent(e.target.value)} />
          <br />
          <div className="tone-selector">
            <label>Select tone:</label>
            <select className="modern-select" value={tone} onChange={(e) => setTone(e.target.value)}>
              <option value="casual">Casual</option>
              <option value="professional">Professional</option>
              <option value="friendly">Friendly</option>
            </select>
          </div>
          <br />
          <button 
            className="modern-btn" 
            onClick={generateReply}
            disabled={loading || connectionStatus === 'disconnected'}>
            {loading ? 'Generating...' : 'Generate Reply'}
          </button>
        </div>
        
        {error && (
          <div className="modern-error">
            <p>{error}</p>
            {connectionStatus === 'disconnected' && (
              <button className="retry-btn" onClick={checkBackendConnection}>
                Retry Connection
              </button>
            )}
          </div>
        )}
        
        {reply && (
          <div className="modern-reply">
            <h4>AI Generated Reply:</h4>
            <div className="reply-content">
              {reply.split('\n').map((line, i) => (
                <p key={i}>{line || ' '}</p>
              ))}
            </div>
            <button className="copy-btn" onClick={copyToClipboard}>
              Copy to Clipboard
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default EmailReplyGenerator;
