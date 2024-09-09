import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { useParams } from 'react-router-dom';
import { Typography, TextField, Button, List, ListItem, ListItemText, CircularProgress } from '@mui/material';
import { ErrorBoundary } from 'react-error-boundary';
import TokenService from '../services/TokenService';
import { getComments, addComment } from '../services/StockApi';

const ErrorFallback = ({ error }) => (
  <div role="alert">
    <p>Something went wrong:</p>
    <pre>{error.message}</pre>
  </div>
);

const CommentForm = React.memo(({ stockId, onCommentAdded }) => {
  const [newComment, setNewComment] = useState('');

  const handleAddComment = useCallback(async () => {
    if (newComment.trim()) {
      try {
        const response = await addComment(stockId, newComment);
        onCommentAdded(response);
        setNewComment('');
      } catch (error) {
        console.error('Error adding comment:', error);
      }
    }
  }, [newComment, stockId, onCommentAdded]);

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
  const { stockId } = useParams();

  const fetchComments = useCallback(async () => {
    try {
      setLoading(true);
      const response = await getComments(stockId, page);
      setComments(prevComments => [...prevComments, ...response.content]);
      setHasMore(!response.last);
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

  const isAuthenticated = TokenService.getToken() !== null;

  return (
    <ErrorBoundary FallbackComponent={ErrorFallback}>
      <div>
        <Typography variant="h5">종목 토론실</Typography>
        {isAuthenticated ? (
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