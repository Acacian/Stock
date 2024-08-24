import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getFollowers, getFollowing } from '../services/SocialApi';
import FollowButton from './FollowButton';

const FollowList = ({ userId, type, currentUserId }) => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        setLoading(true);
        const data = type === 'followers' 
          ? await getFollowers(userId)
          : await getFollowing(userId);
        setUsers(data);
      } catch (error) {
        console.error('Error fetching users:', error);
        setError('Failed to load users');
      } finally {
        setLoading(false);
      }
    };
    fetchUsers();
  }, [userId, type]);

  if (loading) return <div>Loading...</div>;
  if (error) return <div>{error}</div>;

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
                initialIsFollowing={user.isFollowing}
              />
            )}
          </li>
        ))}
      </ul>
    </div>
  );
};

export default FollowList;