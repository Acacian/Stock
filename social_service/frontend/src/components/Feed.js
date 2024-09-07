import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import Post from './Post';
import CreatePost from './CreatePost';

const Feed = () => {
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { user, socialActions } = useAuth();

  const fetchPosts = async () => {
    try {
      setLoading(true);
      const fetchedPosts = await socialActions.getPostsWithActivity();
      setPosts(fetchedPosts);
    } catch (error) {
      console.error('Error fetching posts:', error);
      setError('Failed to load posts');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPosts();
  }, []);

  if (loading) return <div>Loading...</div>;
  if (error) return <div>{error}</div>;

  return (
    <div className="feed">
      <CreatePost onPostCreated={fetchPosts} />
      {posts.map(post => (
        <Post key={post.id} post={post} />
      ))}
    </div>
  );
};

export default Feed;