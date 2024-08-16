import React, { useState, useEffect } from 'react';

const SocialComponent = ({ userId }) => {
  const [posts, setPosts] = useState([]);
  const [newPostContent, setNewPostContent] = useState('');

  useEffect(() => {
    fetchPosts();
  }, [userId]);

  const fetchPosts = async () => {
    try {
      const response = await fetch(`/api/social/posts/user/${userId}`);
      if (!response.ok) {
        throw new Error('Network response was not ok');
      }
      const data = await response.json();
      setPosts(data);
    } catch (error) {
      console.error('Error fetching posts:', error);
    }
  };

  const createPost = async () => {
    try {
      const response = await fetch('/api/social/posts', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ userId, content: newPostContent }),
      });
      if (!response.ok) {
        throw new Error('Network response was not ok');
      }
      setNewPostContent('');
      fetchPosts();
    } catch (error) {
      console.error('Error creating post:', error);
    }
  };

  const likePost = async (postId) => {
    try {
      const response = await fetch(`/api/social/posts/${postId}/likes?userId=${userId}`, {
        method: 'POST',
      });
      if (!response.ok) {
        throw new Error('Network response was not ok');
      }
      fetchPosts();
    } catch (error) {
      console.error('Error liking post:', error);
    }
  };

  return (
    <div className="social">
      <h2>Social Feed</h2>
      <div className="create-post">
        <textarea
          value={newPostContent}
          onChange={(e) => setNewPostContent(e.target.value)}
          placeholder="What's on your mind?"
        />
        <button onClick={createPost}>Post</button>
      </div>
      {posts.map((post) => (
        <div key={post.id} className="post">
          <p>{post.content}</p>
          <button onClick={() => likePost(post.id)}>Like ({post.likes ? post.likes.length : 0})</button>
        </div>
      ))}
    </div>
  );
};

export default SocialComponent;