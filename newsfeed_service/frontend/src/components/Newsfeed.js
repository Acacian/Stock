import React from 'react';

const NewsfeedItem = ({ item, currentUserId }) => {
  const renderContent = () => {
    switch (item.type) {
      case 'POST':
        return (
          <div>
            <p>{item.content}</p>
            <button onClick={() => handleAction('LIKE', item.id)}>
              Like ({item.likeCount})
            </button>
            <button onClick={() => handleAction('COMMENT', item.id)}>
              Comment ({item.commentCount})
            </button>
          </div>
        );
      case 'COMMENT':
        return (
          <p>
            <span className="user-name">{item.userName}</span> commented on a post: "{item.content}"
          </p>
        );
      case 'LIKE':
        return (
          <p>
            <span className="user-name">{item.userName}</span> liked a post
          </p>
        );
      case 'FOLLOW':
        return (
          <p>
            <span className="user-name">{item.userName}</span> followed <span className="user-name">{item.targetUserName}</span>
            {item.targetUserId !== currentUserId && (
              <button onClick={() => handleAction('FOLLOW', item.targetUserId)}>
                Follow
              </button>
            )}
          </p>
        );
      default:
        return <p>{item.content}</p>;
    }
  };

  const handleAction = (action, targetId) => {
    // Send message to social service
    window.parent.postMessage({
      type: 'SOCIAL_ACTION',
      action: action,
      targetId: targetId,
      userId: currentUserId
    }, '*');
  };

  return (
    <div className="newsfeed-item">
      {renderContent()}
      <small>Posted at: {new Date(item.timestamp).toLocaleString()}</small>
    </div>
  );
};

export default NewsfeedItem;