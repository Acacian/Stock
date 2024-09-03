import React, { useState, useEffect } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const SOCKET_URL = process.env.REACT_APP_SOCKET_URL;

const ChatComponent = () => {
  const [stompClient, setStompClient] = useState(null);
  const [messages, setMessages] = useState([]);
  const [message, setMessage] = useState('');

  useEffect(() => {
    const sock = new SockJS(SOCKET_URL);
    const client = Client(sock);
    client.connect({}, () => {
      client.subscribe('/topic/public', onMessageReceived);
    });
    setStompClient(client);

    return () => {
      client.disconnect(() => {
        console.log("Disconnected");
      });
    };
  }, []);

  const onMessageReceived = (payload) => {
    const message = JSON.parse(payload.body);
    setMessages(prevMessages => [...prevMessages, message]);
  };

  const sendMessage = () => {
    if (stompClient && message.trim() !== '') {
      const chatMessage = {
        content: message,
        sender: "user123", // replace with actual sender info
      };
      stompClient.send('/app/chat.send', {}, JSON.stringify(chatMessage));
      setMessage('');
    }
  };

  return (
    <div>
      <div className="chat-box">
        {messages.map((msg, index) => (
          <div key={index}>{msg.sender}: {msg.content}</div>
        ))}
      </div>
      <div className="chat-input">
        <input
          type="text"
          value={message}
          onChange={(e) => setMessage(e.target.value)}
        />
        <button onClick={sendMessage}>Send</button>
      </div>
    </div>
  );
};

export default ChatComponent;
