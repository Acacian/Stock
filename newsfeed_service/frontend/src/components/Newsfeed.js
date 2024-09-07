import React, { useEffect } from 'react';
import NewsfeedItem from './NewsfeedItem';
import { useAuth } from '../context/AuthContext';

const Newsfeed = () => {
  const { user, newsfeed, fetchNewsfeed } = useAuth();

  useEffect(() => {
    if (user) {
      fetchNewsfeed(user.id);
    }
  }, [user, fetchNewsfeed]);

  const handleAction = (action, targetId) => {
    // Send message to social service
    window.parent.postMessage({
      type: 'SOCIAL_ACTION',
      action: action,
      targetId: targetId,
      userId: user.id
    }, '*');
  };

  return (
    <div className="newsfeed-container">
      {newsfeed.map((item, index) => (
        <NewsfeedItem 
          key={index} 
          item={item} 
          currentUserId={user.id}
          onAction={handleAction}
        />
      ))}
    </div>
  );
};

export default Newsfeed;