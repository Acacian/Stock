import React, { useState } from 'react';
import SocialApi from '../services/SocialApi';

const FollowButton = ({ targetUserId, initialIsFollowing }) => {
  const [isFollowing, setIsFollowing] = useState(initialIsFollowing);

  const handleFollow = async () => {
    try {
      if (isFollowing) {
        await SocialApi.unfollow(targetUserId);
      } else {
        await SocialApi.follow(targetUserId);
      }
      setIsFollowing(!isFollowing);
    } catch (error) {
      console.error('Error following/unfollowing:', error);
    }
  };

  return (
    <button onClick={handleFollow}>
      {isFollowing ? 'Unfollow' : 'Follow'}
    </button>
  );
};

export default FollowButton;