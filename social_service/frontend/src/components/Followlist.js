import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import FollowButton from './FollowButton';

const FollowList = ({ type }) => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { user, socialActions } = useAuth();

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        setLoading(true);
        const data = type === 'followers' 
          ? await socialActions.getFollowers()
          : await socialActions.getFollowing();
        setUsers(data);
      } catch (error) {
        console.error('Error fetching users:', error);
        setError('Failed to load users');
      } finally {
        setLoading(false);
      }
    };
    fetchUsers();
  }, [type, socialActions]);

  if (loading) return <div>Loading...</div>;
  if (error) return <div>{error}</div>;

  return (
    <div className="follow-list">
      <h2>{type === 'followers' ? 'Followers' : 'Following'}</h2>
      <ul>
        {users.map(otherUser => (
          <li key={otherUser.id}>
            <Link to={`/profile/${otherUser.id}`}>{otherUser.name}</Link>
            {user.id !== otherUser.id && (
              <FollowButton 
                targetUserId={otherUser.id}
                initialIsFollowing={otherUser.isFollowing}
              />
            )}
          </li>
        ))}
      </ul>
    </div>
  );
};

export default FollowList;