import React from 'react';

const NewsfeedItem = ({ item, currentUserId }) => {
  const handleAction = (action, targetId) => {
    window.parent.postMessage({
      type: 'SOCIAL_ACTION',
      action: action,
      targetId: targetId,
      userId: currentUserId
    }, '*');
  };

  const renderContent = () => {
    switch (item.type) {
      case 'POST':
        return (
          <div>
            <p><strong>{item.userName}</strong> posted: {item.content}</p>
            <button onClick={() => handleAction('LIKE', item.targetId)}>
              Like ({item.likeCount})
            </button>
            <button onClick={() => handleAction('COMMENT', item.targetId)}>
              Comment ({item.commentCount})
            </button>
          </div>
        );
      case 'COMMENT':
        return (
          <p>
            <strong>{item.userName}</strong> commented on a post: "{item.content}"
          </p>
        );
      case 'LIKE':
        return (
          <p>
            <strong>{item.userName}</strong> liked a post
          </p>
        );
      case 'FOLLOW':
        return (
          <p>
            <strong>{item.userName}</strong> followed <strong>{item.targetUserName}</strong>
            {item.targetId !== currentUserId && (
              <button onClick={() => handleAction('FOLLOW', item.targetId)}>
                Follow
              </button>
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
      <small>{new Date(item.timestamp).toLocaleString()}</small>
    </div>
  );
};

export default NewsfeedItem;