import React, { useState } from 'react';
import { follow, unfollow } from '../services/SocialApi';

const FollowButton = ({ currentUserId, targetUserId, initialIsFollowing }) => {
  const [isFollowing, setIsFollowing] = useState(initialIsFollowing);

  const handleToggleFollow = async () => {
    try {
      if (isFollowing) {
        await unfollow(currentUserId, targetUserId);
      } else {
        await follow(currentUserId, targetUserId);
      }
      setIsFollowing(!isFollowing);
      
      // Notify newsfeed service
      window.parent.postMessage({
        type: 'FOLLOW_ACTION',
        action: isFollowing ? 'UNFOLLOW' : 'FOLLOW',
        currentUserId,
        targetUserId
      }, '*');
    } catch (error) {
      console.error('Failed to toggle follow:', error);
    }
  };

  return (
    <button onClick={handleToggleFollow}>
      {isFollowing ? 'Unfollow' : 'Follow'}
    </button>
  );
};

export default FollowButton;