import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getFollowers, getFollowing } from '../../services/SocialApi';
import { FollowButton } from './FollowButton';

const FollowList = ({ userId, type, currentUserId }) => {
  const [users, setUsers] = useState([]);

  useEffect(() => {
    const fetchUsers = async () => {
      const data = type === 'followers' 
        ? await getFollowers(userId)
        : await getFollowing(userId);
      setUsers(data);
    };
    fetchUsers();
  }, [userId, type]);

  return (
    <div className="follow-list">
      <h2>{type === 'followers' ? 'Followers' : 'Following'}</h2>
      <ul>
        {users.map(user => (
          <li key={user.id}>
            <Link to={`/profile/${user.id}`}>{user.name}</Link>
            {currentUserId !== user.id && (
              <FollowButton 
                currentUserId={currentUserId} 
                targetUserId={user.id} 
              />
            )}
          </li>
        ))}
      </ul>
    </div>
  );
};

export default FollowList;