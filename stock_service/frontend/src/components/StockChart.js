import React, { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { Typography, CircularProgress, Button } from '@material-ui/core';
import { getStockPrices } from '../services/stockapi';

const StockChart = ({ stockId }) => {
  const [prices, setPrices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [period, setPeriod] = useState('daily');

  useEffect(() => {
    fetchStockPrices();
  }, [stockId, period]);

  const fetchStockPrices = async () => {
    try {
      setLoading(true);
      const data = await getStockPrices(stockId, period);
      setPrices(data);
    } catch (error) {
      console.error('Error fetching stock prices:', error);
      setError('주가 데이터를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
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
        <Button onClick={() => setPeriod('daily')} color={period === 'daily' ? 'primary' : 'default'}>일</Button>
        <Button onClick={() => setPeriod('weekly')} color={period === 'weekly' ? 'primary' : 'default'}>주</Button>
        <Button onClick={() => setPeriod('monthly')} color={period === 'monthly' ? 'primary' : 'default'}>월</Button>
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