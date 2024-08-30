import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Typography, Paper, CircularProgress, Button } from '@mui/material';
import StockChart from './StockChart';
import StockDiscussion from './StockDiscussion';
import { getStockById, getStockPrices, getTechnicalIndicator } from '../services/StockApi';

const StockDetail = () => {
  const { id } = useParams();
  const [stock, setStock] = useState(null);
  const [priceData, setPriceData] = useState(null);
  const [indicatorData, setIndicatorData] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [period, setPeriod] = useState('daily');
  const [selectedIndicator, setSelectedIndicator] = useState('none');

  useEffect(() => {
    const fetchStockDetails = async () => {
      try {
        setLoading(true);
        const stockData = await getStockById(id);
        setStock(stockData);
        const priceData = await getStockPrices(id, period);
        setPriceData(priceData);
      } catch (error) {
        console.error('Error fetching stock details:', error);
        setError('주식 정보를 불러오는데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };

    fetchStockDetails();
  }, [id, period]);

  useEffect(() => {
    const fetchIndicatorData = async () => {
      if (selectedIndicator !== 'none') {
        try {
          setLoading(true);
          const data = await getTechnicalIndicator(id, selectedIndicator);
          setIndicatorData(prevData => ({ ...prevData, [selectedIndicator]: data }));
        } catch (error) {
          console.error(`Error fetching ${selectedIndicator} data:`, error);
          setError(`${selectedIndicator} 데이터를 불러오는데 실패했습니다.`);
        } finally {
          setLoading(false);
        }
      }
    };

    fetchIndicatorData();
  }, [id, selectedIndicator]);

  const handlePeriodChange = (newPeriod) => {
    setPeriod(newPeriod);
  };

  const handleIndicatorChange = (indicator) => {
    setSelectedIndicator(indicator);
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
      
      <div>
        <Button onClick={() => handleIndicatorChange('none')}>기본</Button>
        <Button onClick={() => handleIndicatorChange('moving-averages')}>이동평균</Button>
        <Button onClick={() => handleIndicatorChange('bollinger-bands')}>볼린저 밴드</Button>
        <Button onClick={() => handleIndicatorChange('macd')}>MACD</Button>
        <Button onClick={() => handleIndicatorChange('rsi')}>RSI</Button>
      </div>

      {priceData && (
        <StockChart 
          data={priceData} 
          indicatorData={indicatorData[selectedIndicator]}
          period={period} 
          selectedIndicator={selectedIndicator}
          onPeriodChange={handlePeriodChange}
        />
      )}

      <Typography variant="h5" style={{ marginTop: '20px' }}>주가 정보</Typography>
      {priceData && priceData.length > 0 && (
        <div>
          <Typography>최근 종가: {priceData[priceData.length - 1].closePrice.toLocaleString()}원</Typography>
          <Typography>전일 대비: {priceData[priceData.length - 1].changeAmount.toLocaleString()}원 ({priceData[priceData.length - 1].changeRate.toFixed(2)}%)</Typography>
          <Typography>거래량: {priceData[priceData.length - 1].volume.toLocaleString()}</Typography>
        </div>
      )}

      <Typography variant="h5" style={{ marginTop: '20px' }}>종목 토론</Typography>
      <StockDiscussion stockId={id} />
    </Paper>
  );
};

export default StockDetail;