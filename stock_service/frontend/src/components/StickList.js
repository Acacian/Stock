import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import { List, ListItem, ListItemText, TextField, Button, CircularProgress, Typography } from '@material-ui/core';
import { debounce } from 'lodash';

const StockList = () => {
  const [stocks, setStocks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [sortBy, setSortBy] = useState('name');
  const [sortOrder, setSortOrder] = useState('asc');

  const fetchStocks = useCallback(async (query = '') => {
    try {
      setLoading(true);
      const response = await axios.get(`/api/stocks?search=${query}&sort=${sortBy},${sortOrder}`);
      setStocks(response.data.content);
    } catch (error) {
      console.error('Error fetching stocks:', error);
      setError('주식 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, [sortBy, sortOrder]);

  useEffect(() => {
    fetchStocks();
  }, [fetchStocks]);

  const debouncedSearch = useCallback(
    debounce((query) => fetchStocks(query), 300),
    [fetchStocks]
  );

  const handleSearchChange = (e) => {
    setSearchQuery(e.target.value);
    debouncedSearch(e.target.value);
  };

  const handleSort = (field) => {
    setSortBy(field);
    setSortOrder(prevOrder => prevOrder === 'asc' ? 'desc' : 'asc');
  };

  if (error) {
    return <Typography color="error">{error}</Typography>;
  }

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
      {loading ? (
        <CircularProgress />
      ) : (
        <List>
          {stocks.map((stock) => (
            <ListItem key={stock.id} component={Link} to={`/stocks/${stock.id}`}>
              <ListItemText primary={stock.name} secondary={stock.code} />
            </ListItem>
          ))}
        </List>
      )}
    </div>
  );
};

export default StockList;