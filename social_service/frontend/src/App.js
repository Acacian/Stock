import React, { useState, useEffect } from 'react';
import './App.css';

function App() {
  const [posts, setPosts] = useState([]);
  const [newPostContent, setNewPostContent] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchPosts();
  }, []);

  const fetchPosts = async () => {
    try {
      setLoading(true);
      const response = await fetch('/api/social/posts');
      if (!response.ok) {
        throw new Error('Failed to fetch posts');
      }
      const data = await response.json();
      setPosts(data);
      setLoading(false);
    } catch (error) {
      setError('Error fetching posts: ' + error.message);
      setLoading(false);
    }
  };

  const createPost = async () => {
    try {
      const response = await fetch('/api/social/posts', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ content: newPostContent }),
      });
      if (!response.ok) {
        throw new Error('Failed to create post');
      }
      setNewPostContent('');
      fetchPosts();
    } catch (error) {
      setError('Error creating post: ' + error.message);
    }
  };

  const likePost = async (postId) => {
    try {
      const response = await fetch(`/api/social/posts/${postId}/likes`, {
        method: 'POST',
      });
      if (!response.ok) {
        throw new Error('Failed to like post');
      }
      fetchPosts();
    } catch (error) {
      setError('Error liking post: ' + error.message);
    }
  };

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div className="App">
      <h1>Social Feed</h1>
      <div className="create-post">
        <textarea
          value={newPostContent}
          onChange={(e) => setNewPostContent(e.target.value)}
          placeholder="What's on your mind?"
        />
        <button onClick={createPost}>Post</button>
      </div>
      <div className="posts">
        {posts.map((post) => (
          <div key={post.id} className="post">
            <p>{post.content}</p>
            <button onClick={() => likePost(post.id)}>
              Like ({post.likes ? post.likes.length : 0})
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}

export default App;