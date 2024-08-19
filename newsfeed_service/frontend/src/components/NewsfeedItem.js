import React from 'react';
import { Link } from 'react-router-dom';
import { FollowButton } from './FollowButton';

const NewsfeedItem = ({ item, currentUserId }) => {
  const renderContent = () => {
    switch (item.type) {
      case 'POST':
        return (
          <div>
            <p>{item.content}</p>
            <button onClick={() => handleLike(item.id)}>
              Like ({item.likeCount})
            </button>
            <button onClick={() => handleComment(item.id)}>
              Comment ({item.commentCount})
            </button>
          </div>
        );
      case 'COMMENT':
        return (
          <p>
            <Link to={`/profile/${item.userId}`}>{item.userName}</Link> commented on a post: "{item.content}"
          </p>
        );
      case 'LIKE':
        return (
          <p>
            <Link to={`/profile/${item.userId}`}>{item.userName}</Link> liked a post
          </p>
        );
      case 'FOLLOW':
        return (
          <p>
            <Link to={`/profile/${item.userId}`}>{item.userName}</Link> followed <Link to={`/profile/${item.targetUserId}`}>{item.targetUserName}</Link>
            {item.targetUserId !== currentUserId && (
              <FollowButton
                currentUserId={currentUserId}
                targetUserId={item.targetUserId}
                initialIsFollowing={false}
              />
            )}
          </p>
        );
      default:
        return <p>{item.content}</p>;
    }
  };

  return (
    <div className="newsfeed-item">
      {renderContent()}
      <small>Posted at: {new Date(item.timestamp).toLocaleString()}</small>
    </div>
  );
};

export default NewsfeedItem;