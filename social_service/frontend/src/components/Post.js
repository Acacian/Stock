import React, { useState } from 'react';
import { likePost, addComment } from '../services/SocialApi';

const Post = ({ post, currentUserId }) => {
  const [comment, setComment] = useState('');
  const [likeCount, setLikeCount] = useState(post.likeCount);
  const [commentCount, setCommentCount] = useState(post.commentCount);

  const handleLike = async () => {
    try {
      await likePost(currentUserId, post.id);
      setLikeCount(prevCount => prevCount + 1);
    } catch (error) {
      console.error('Error liking post:', error);
    }
  };

  const handleAddComment = async (e) => {
    e.preventDefault();
    try {
      await addComment(currentUserId, post.id, comment);
      setComment('');
      setCommentCount(prevCount => prevCount + 1);
    } catch (error) {
      console.error('Error adding comment:', error);
    }
  };

  return (
    <div className="post">
      <h3>{post.title}</h3>
      <p>{post.content}</p>
      <button onClick={handleLike}>Like ({likeCount})</button>
      <div>
        <h4>Comments ({commentCount})</h4>
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