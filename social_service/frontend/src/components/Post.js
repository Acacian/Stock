import React, { useState } from 'react';
import { likePost, addComment } from '../services/SocialApi';

const Post = ({ post, currentUserId }) => {
  const [comment, setComment] = useState('');

  const handleLike = () => likePost(currentUserId, post.id);

  const handleAddComment = async (e) => {
    e.preventDefault();
    await addComment(currentUserId, post.id, comment);
    setComment('');
  };

  return (
    <div className="post">
      <p>{post.content}</p>
      <button onClick={handleLike}>Like ({post.likeCount})</button>
      <div>
        <h4>Comments ({post.commentCount})</h4>
        <form onSubmit={handleAddComment}>
          <input
            type="text"
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            placeholder="Add a comment"
          />
          <button type="submit">Comment</button>
        </form>
      </div>
    </div>
  );
};

export default Post;