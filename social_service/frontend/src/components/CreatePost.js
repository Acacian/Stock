import React, { useState } from 'react';
import { createPost } from '../services/SocialApi';

const CreatePost = ({ userId, onPostCreated }) => {
  const [content, setContent] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    await createPost(userId, content);
    setContent('');
    onPostCreated();
  };

  return (
    <form onSubmit={handleSubmit}>
      <textarea
        value={content}
        onChange={(e) => setContent(e.target.value)}
        placeholder="What's on your mind?"
      />
      <button type="submit">Post</button>
    </form>
  );
};

export default CreatePost;