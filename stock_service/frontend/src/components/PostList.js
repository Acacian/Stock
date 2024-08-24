import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { List, ListItem, ListItemText, CircularProgress, Typography, Button, Select, MenuItem } from '@material-ui/core';
import ThumbUpIcon from '@material-ui/icons/ThumbUp';
import { getAllStocks } from '../api/stockapi';

const PostList = () => {
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [stocks, setStocks] = useState([]);
  const [selectedStock, setSelectedStock] = useState('');

  useEffect(() => {
    fetchStocks();
    fetchPosts();
  }, []);

  const fetchStocks = async () => {
    try {
      const data = await getAllStocks();
      setStocks(data.content);
    } catch (error) {
      console.error('Error fetching stocks:', error);
    }
  };

  const fetchPosts = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`/api/social/posts`, {
        params: {
          page,
          size: 10,
          stockId: selectedStock
        }
      });
      setPosts(prevPosts => [...prevPosts, ...response.data.content]);
      setHasMore(!response.data.last);
      setPage(prevPage => prevPage + 1);
    } catch (error) {
      console.error('Error fetching posts:', error);
      setError('포스트를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleLike = async (postId) => {
    try {
      await axios.post(`/api/social/posts/${postId}/like`);
      setPosts(prevPosts => 
        prevPosts.map(post => 
          post.id === postId ? { ...post, likeCount: post.likeCount + 1 } : post
        )
      );
    } catch (error) {
      console.error('Error liking post:', error);
    }
  };

  const handleStockChange = (event) => {
    setSelectedStock(event.target.value);
    setPage(0);
    setPosts([]);
    fetchPosts();
  };

  if (error) {
    return <Typography color="error">{error}</Typography>;
  }

  return (
    <div className="post-list">
      <Typography variant="h4">최근 포스트</Typography>
      <Select
        value={selectedStock}
        onChange={handleStockChange}
        displayEmpty
        fullWidth
      >
        <MenuItem value="">모든 종목</MenuItem>
        {stocks.map((stock) => (
          <MenuItem key={stock.id} value={stock.id}>{stock.name}</MenuItem>
        ))}
      </Select>
      <List>
        {posts.map(post => (
          <ListItem key={post.id}>
            <ListItemText
              primary={post.title}
              secondary={
                <>
                  <Typography component="span" variant="body2" color="textPrimary">
                    {post.content}
                  </Typography>
                  <br />
                  {`작성자: ${post.authorName} | ${new Date(post.createdAt).toLocaleString()} | 종목: ${post.stockName}`}
                </>
              }
            />
            <Button 
              startIcon={<ThumbUpIcon />} 
              onClick={() => handleLike(post.id)}
            >
              {post.likeCount}
            </Button>
          </ListItem>
        ))}
      </List>
      {loading && <CircularProgress />}
      {!loading && hasMore && (
        <Button onClick={fetchPosts}>더 보기</Button>
      )}
    </div>
  );
};

export default PostList;