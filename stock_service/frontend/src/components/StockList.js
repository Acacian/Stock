import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { List, ListItem, ListItemText, TextField, Button, CircularProgress, Typography, Select, MenuItem } from '@mui/material';
import { debounce } from 'lodash';
import { getAllStocks, searchStocks, getStocksSortedByYesterdayTrading, getStocksSortedByYesterdayChangeRate } from '../services/StockApi';

const StockList = () => {
  const [stocks, setStocks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [sortBy, setSortBy] = useState('name');
  const [sortOrder, setSortOrder] = useState('asc');
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [sortType, setSortType] = useState('name');

  const fetchStocks = useCallback(async (query = '') => {
    try {
      setLoading(true);
      let data;
      if (query) {
        data = await searchStocks(query, page, 10, `${sortBy},${sortOrder}`);
      } else if (sortType === 'yesterdayTrading') {
        data = await getStocksSortedByYesterdayTrading(page, 10, sortOrder);
      } else if (sortType === 'yesterdayChangeRate') {
        data = await getStocksSortedByYesterdayChangeRate(page, 10, sortOrder);
      } else {
        data = await getAllStocks(page, 10, `${sortBy},${sortOrder}`);
      }
      setStocks(prevStocks => page === 0 ? data.content : [...prevStocks, ...data.content]);
      setHasMore(!data.last);
      setPage(prevPage => prevPage + 1);
    } catch (error) {
      console.error('Error fetching stocks:', error);
      setError('주식 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, [sortBy, sortOrder, sortType, page]);

  useEffect(() => {
    fetchStocks();
  }, [fetchStocks]);

  const debouncedSearch = useCallback(
    debounce((query) => {
      setStocks([]);
      setPage(0);
      fetchStocks(query);
    }, 300),
    [fetchStocks]
  );

  const handleSearchChange = (e) => {
    setSearchQuery(e.target.value);
    debouncedSearch(e.target.value);
  };

  const handleSort = (field) => {
    setSortBy(field);
    setSortOrder(prevOrder => prevOrder === 'asc' ? 'desc' : 'asc');
    setStocks([]);
    setPage(0);
  };

  const handleSortOrderChange = (event) => {
    setSortOrder(event.target.value);
    setStocks([]);
    setPage(0);
  };

  const handleSortTypeChange = (event) => {
    setSortType(event.target.value);
    setStocks([]);
    setPage(0);
  };

  return (
    <div>
      <Typography variant="h4">주식 목록</Typography>
      <TextField
        value={searchQuery}
        onChange={handleSearchChange}
        placeholder="종목 검색"
      />
      <Select
        value={sortType}
        onChange={handleSortTypeChange}
        displayEmpty
        inputProps={{ 'aria-label': '정렬 기준' }}
      >
        <MenuItem value="name">이름순</MenuItem>
        <MenuItem value="code">코드순</MenuItem>
        <MenuItem value="marketCap">시가총액순</MenuItem>
        <MenuItem value="yesterdayTrading">어제의 거래금액순</MenuItem>
        <MenuItem value="yesterdayChangeRate">어제의 등락률순</MenuItem>
      </Select>
      <Select
        value={sortOrder}
        onChange={handleSortOrderChange}
        displayEmpty
        inputProps={{ 'aria-label': '정렬 순서' }}
      >
        <MenuItem value="asc">오름차순</MenuItem>
        <MenuItem value="desc">내림차순</MenuItem>
      </Select>
      <List>
        {stocks.map((stock) => (
          <ListItem key={stock.id} component={Link} to={`/stocks/${stock.id}`}>
            <ListItemText 
              primary={stock.name} 
              secondary={`코드: ${stock.code} | 시가총액: ${stock.marketCap.toLocaleString()}원`} 
            />
          </ListItem>
        ))}
      </List>
      {loading && <CircularProgress />}
      {!loading && hasMore && (
        <Button onClick={() => fetchStocks(searchQuery)}>더 보기</Button>
      )}
      {error && <Typography color="error">{error}</Typography>}
    </div>
  );
};

export default StockList;