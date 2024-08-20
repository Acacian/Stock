import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import { Typography, Paper, CircularProgress } from '@material-ui/core';
import StockChart from './StockChart';
import StockDiscussion from './StockDiscussion';

const StockDetail = () => {
  const { id } = useParams();
  const [stock, setStock] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchStockDetails();
  }, [id]);

  const fetchStockDetails = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`/api/stocks/${id}`);
      setStock(response.data);
    } catch (error) {
      console.error('Error fetching stock details:', error);
      setError('주식 정보를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <CircularProgress />;
  if (error) return <Typography color="error">{error}</Typography>;
  if (!stock) return null;

  return (
    <Paper style={{ padding: '20px' }}>
      <Typography variant="h4">{stock.name} ({stock.code})</Typography>
      <Typography variant="subtitle1">시장: {stock.marketType}</Typography>
      <Typography variant="body1">섹터: {stock.sector}</Typography>
      <Typography variant="body1">시가총액: {stock.marketCap.toLocaleString()}원</Typography>
      <StockChart stockId={id} />
      <StockDiscussion stockId={id} />
    </Paper>
  );
};

export default StockDetail;