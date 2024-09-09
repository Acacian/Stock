import React, { useState, useEffect } from 'react';
import NewsfeedItem from './NewsfeedItem';
import NewsfeedApi from '../services/NewsfeedApi';
import TokenService from '../services/TokenService';

const Newsfeed = () => {
  const [newsfeed, setNewsfeed] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchNewsfeedData = async () => {
      try {
        setLoading(true);
        const data = await NewsfeedApi.fetchNewsfeed();
        setNewsfeed(data);
      } catch (error) {
        console.error('Failed to fetch newsfeed:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchNewsfeedData();
  }, []);

  const handleAction = (action, targetId) => {
    // Send message to social service
    window.parent.postMessage({
      type: 'SOCIAL_ACTION',
      action: action,
      targetId: targetId,
      userId: TokenService.getUserId()
    }, '*');
  };

  if (loading) {
    return <div>Loading newsfeed...</div>;
  }

  return (
    <div className="newsfeed-container">
      {newsfeed.map((item, index) => (
        <NewsfeedItem 
          key={index} 
          item={item} 
          currentUserId={TokenService.getUserId()}
          onAction={handleAction}
        />
      ))}
    </div>
  );
};

export default Newsfeed;