import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { List, ListItem, ListItemText, TextField, Button, CircularProgress, Typography } from '@material-ui/core';
import { debounce } from 'lodash';
import { getAllStocks, searchStocks } from '../services/StockApi';

const StockList = () => {
  const [stocks, setStocks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [sortBy, setSortBy] = useState('name');
  const [sortOrder, setSortOrder] = useState('asc');
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);

  const fetchStocks = useCallback(async (query = '') => {
    try {
      setLoading(true);
      let data;
      if (query) {
        data = await searchStocks(query, page);
      } else {
        data = await getAllStocks(page, 10, `${sortBy},${sortOrder}`);
      }
      setStocks(prevStocks => [...prevStocks, ...data.content]);
      setHasMore(!data.last);
      setPage(prevPage => prevPage + 1);
    } catch (error) {
      console.error('Error fetching stocks:', error);
      setError('주식 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, [sortBy, sortOrder, page]);

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

  return (
    <div>
      <Typography variant="h4">주식 목록</Typography>
      <TextField
        value={searchQuery}
        onChange={handleSearchChange}
        placeholder="종목 검색"
      />
      <Button onClick={() => handleSort('name')}>이름순</Button>
      <Button onClick={() => handleSort('code')}>코드순</Button>
      <List>
        {stocks.map((stock) => (
          <ListItem key={stock.id} component={Link} to={`/stocks/${stock.id}`}>
            <ListItemText primary={stock.name} secondary={stock.code} />
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
