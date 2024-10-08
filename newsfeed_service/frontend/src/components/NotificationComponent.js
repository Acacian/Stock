import React, { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import NewsfeedItem from './NewsfeedItem';

const WEBSOCKET_URL = process.env.REACT_APP_SOCKET_URL || 'wss://localhost:8081/ws';

const NotificationComponent = ({ currentUserId }) => {
  const [notifications, setNotifications] = useState([]);

  useEffect(() => {
    const socket = new SockJS(WEBSOCKET_URL);
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
      stompClient.subscribe(`/topic/newsfeed/${currentUserId}`, (message) => {
        const newNotification = JSON.parse(message.body);
        setNotifications(prev => [newNotification, ...prev].slice(0, 5));
      });
    });

    return () => {
      if (stompClient.connected) {
        stompClient.disconnect();
      }
    };
  }, [currentUserId]);

  return (
    <div className="notification-container">
      {notifications.map((notification, index) => (
        <NewsfeedItem key={index} item={notification} currentUserId={currentUserId} />
      ))}
    </div>
  );
};

export default NotificationComponent;