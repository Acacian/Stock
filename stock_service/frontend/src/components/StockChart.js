import React from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { Typography, Button } from '@mui/material';

const StockChart = ({ stockId, data, period, onPeriodChange }) => {
  if (!data || data.length === 0) {
    return <Typography>데이터가 없습니다.</Typography>;
  }

  return (
    <div>
      <div>
        <Button onClick={() => onPeriodChange('daily')} color={period === 'daily' ? 'primary' : 'default'}>일</Button>
        <Button onClick={() => onPeriodChange('weekly')} color={period === 'weekly' ? 'primary' : 'default'}>주</Button>
        <Button onClick={() => onPeriodChange('monthly')} color={period === 'monthly' ? 'primary' : 'default'}>월</Button>
      </div>
      <ResponsiveContainer width="100%" height={300}>
        <LineChart data={data}>
          <XAxis dataKey="date" />
          <YAxis yAxisId="left" />
          <YAxis yAxisId="right" orientation="right" />
          <CartesianGrid strokeDasharray="3 3" />
          <Tooltip />
          <Legend />
          <Line type="monotone" dataKey="closePrice" stroke="#8884d8" yAxisId="left" />
          <Line type="monotone" dataKey="volume" stroke="#82ca9d" yAxisId="right" />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
};

export default StockChart;