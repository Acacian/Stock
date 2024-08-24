import React, { useState } from 'react';
import { follow, unfollow } from '../services/SocialApi';

const FollowButton = ({ currentUserId, targetUserId, initialIsFollowing }) => {
  const [isFollowing, setIsFollowing] = useState(initialIsFollowing);
  const [loading, setLoading] = useState(false);

  const handleToggleFollow = async () => {
    try {
      setLoading(true);
      if (isFollowing) {
        await unfollow(currentUserId, targetUserId);
      } else {
        await follow(currentUserId, targetUserId);
      }
      setIsFollowing(!isFollowing);
    } catch (error) {
      console.error('Failed to toggle follow:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <button onClick={handleToggleFollow} disabled={loading}>
      {isFollowing ? 'Unfollow' : 'Follow'}
    </button>
  );
};

export default FollowButton;