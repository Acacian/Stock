import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { List, ListItem, ListItemText, CircularProgress, Typography, Button } from '@material-ui/core';

const CommentList = ({ postId }) => {
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);

  const fetchComments = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`/api/social/posts/${postId}/comments?page=${page}&size=10`);
      setComments(prevComments => [...prevComments, ...response.data.content]);
      setHasMore(!response.data.last);
      setPage(prevPage => prevPage + 1);
    } catch (error) {
      console.error('Error fetching comments:', error);
      setError('댓글을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchComments();
  }, [postId]);

  if (error) {
    return <Typography color="error">{error}</Typography>;
  }

  return (
    <div className="comment-list">
      <Typography variant="h6">댓글</Typography>
      <List>
        {comments.map(comment => (
          <ListItem key={comment.id}>
            <ListItemText 
              primary={comment.content}
              secondary={`작성자: ${comment.authorName} | ${new Date(comment.createdAt).toLocaleString()}`}
            />
          </ListItem>
        ))}
      </List>
      {loading && <CircularProgress />}
      {!loading && hasMore && (
        <Button onClick={fetchComments}>더 보기</Button>
      )}
    </div>
  );
};

export default CommentList;