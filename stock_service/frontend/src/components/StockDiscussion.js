import React, { useState, useEffect, useCallback, useMemo } from 'react';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';
import { useParams } from 'react-router-dom';
import { Typography, TextField, Button, List, ListItem, ListItemText, CircularProgress } from '@mui/material';
import { ErrorBoundary } from 'react-error-boundary';

const ErrorFallback = ({ error }) => (
  <div role="alert">
    <p>Something went wrong:</p>
    <pre>{error.message}</pre>
  </div>
);

const CommentForm = React.memo(({ stockId, onCommentAdded }) => {
  const [newComment, setNewComment] = useState('');
  const { user } = useAuth();

  const handleAddComment = useCallback(async () => {
    if (newComment.trim() && user) {
      try {
        const response = await axios.post(`/api/social/posts/${stockId}/comments`, {
          userId: user.id,
          content: newComment
        });
        onCommentAdded(response.data);
        setNewComment('');
      } catch (error) {
        console.error('Error adding comment:', error);
      }
    }
  }, [newComment, user, stockId, onCommentAdded]);

  return (
    <div>
      <TextField
        value={newComment}
        onChange={(e) => setNewComment(e.target.value)}
        placeholder="댓글 작성"
        multiline
        rows={3}
        fullWidth
        aria-label="댓글 입력"
      />
      <Button 
        onClick={handleAddComment} 
        disabled={!newComment.trim()}
        aria-label="댓글 작성"
      >
        작성
      </Button>
    </div>
  );
});

const Comment = React.memo(({ comment }) => (
  <ListItem>
    <ListItemText
      primary={comment.content}
      secondary={`작성자: ${comment.userId} | 작성일: ${new Date(comment.createdAt).toLocaleString()}`}
    />
  </ListItem>
));

const StockDiscussion = () => {
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const { user } = useAuth();
  const { stockId } = useParams();

  const fetchComments = useCallback(async () => {
    try {
      setLoading(true);
      const response = await axios.get(`/api/social/posts/${stockId}/comments?page=${page}&size=10`);
      setComments(prevComments => [...prevComments, ...response.data.content]);
      setHasMore(!response.data.last);
      setPage(prevPage => prevPage + 1);
    } catch (error) {
      console.error('Error fetching comments:', error);
    } finally {
      setLoading(false);
    }
  }, [stockId, page]);

  useEffect(() => {
    fetchComments();
  }, [fetchComments]);

  const handleCommentAdded = useCallback((newComment) => {
    setComments(prevComments => [newComment, ...prevComments]);
  }, []);

  const memoizedComments = useMemo(() => (
    <List>
      {comments.map((comment) => (
        <Comment key={comment.id} comment={comment} />
      ))}
    </List>
  ), [comments]);

  return (
    <ErrorBoundary FallbackComponent={ErrorFallback}>
      <div>
        <Typography variant="h5">종목 토론실</Typography>
        {user ? (
          <CommentForm stockId={stockId} onCommentAdded={handleCommentAdded} />
        ) : (
          <Typography>댓글을 작성하려면 로그인이 필요합니다.</Typography>
        )}
        {memoizedComments}
        {loading && <CircularProgress aria-label="로딩 중" />}
        {!loading && hasMore && (
          <Button onClick={fetchComments} aria-label="더 보기">더 보기</Button>
        )}
      </div>
    </ErrorBoundary>
  );
};

export default React.memo(StockDiscussion);