import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { Typography, CircularProgress, Button } from '@material-ui/core';

const StockChart = ({ stockId }) => {
  const [prices, setPrices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [period, setPeriod] = useState('1M'); // 1M, 3M, 6M, 1Y

  useEffect(() => {
    fetchStockPrices();
  }, [stockId, period]);

  const fetchStockPrices = async () => {
    setLoading(true);
    const endDate = new Date().toISOString().split('T')[0];
    const startDate = getStartDate(period);
    try {
      const response = await axios.get(`/api/stocks/${stockId}/prices?startDate=${startDate}&endDate=${endDate}`);
      setPrices(response.data);
    } catch (error) {
      console.error('Error fetching stock prices:', error);
      setError('주가 데이터를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const getStartDate = (period) => {
    const date = new Date();
    switch (period) {
      case '3M':
        date.setMonth(date.getMonth() - 3);
        break;
      case '6M':
        date.setMonth(date.getMonth() - 6);
        break;
      case '1Y':
        date.setFullYear(date.getFullYear() - 1);
        break;
      default: // 1M
        date.setMonth(date.getMonth() - 1);
    }
    return date.toISOString().split('T')[0];
  };

  if (error) {
    return <Typography color="error">{error}</Typography>;
  }

  if (loading) {
    return <CircularProgress />;
  }

  return (
    <div>
      <div>
        <Button onClick={() => setPeriod('1M')}>1개월</Button>
        <Button onClick={() => setPeriod('3M')}>3개월</Button>
        <Button onClick={() => setPeriod('6M')}>6개월</Button>
        <Button onClick={() => setPeriod('1Y')}>1년</Button>
      </div>
      <ResponsiveContainer width="100%" height={300}>
        <LineChart data={prices}>
          <XAxis dataKey="date" />
          <YAxis />
          <CartesianGrid strokeDasharray="3 3" />
          <Tooltip />
          <Legend />
          <Line type="monotone" dataKey="closePrice" stroke="#8884d8" />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
};

export default StockChart;