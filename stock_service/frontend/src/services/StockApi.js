import axios from 'axios';

const API_URL = process.env.REACT_APP_STOCK_API_URL || '/api/stock';

const axiosInstance = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

const handleApiError = (error) => {
  if (error.response) {
    throw new Error(error.response.data.message || '서버 오류가 발생했습니다.');
  } else if (error.request) {
    throw new Error('서버에 연결할 수 없습니다. 네트워크 연결을 확인해 주세요.');
  } else {
    throw new Error('예기치 않은 오류가 발생했습니다.');
  }
};

export const getAllStocks = async (page = 0, size = 10, sort = 'name,asc') => {
  try {
    const response = await axiosInstance.get('', { params: { page, size, sort } });
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

export const getStockById = async (id) => {
  try {
    const response = await axiosInstance.get(`/${id}`);
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

export const getStockPrices = async (stockId, period = 'daily', startDate, endDate) => {
  try {
    const response = await axiosInstance.get(`/${stockId}/prices/${period}`, {
      params: { startDate, endDate }
    });
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

export const searchStocks = async (query, page = 0, size = 10, sort = 'name,asc') => {
  try {
    const response = await axiosInstance.get('/search', {
      params: { query, page, size, sort }
    });
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

export const getStocksSortedByYesterdayTrading = async (page = 0, size = 10, direction = 'DESC') => {
  try {
    const response = await axiosInstance.get('/sorted', {
      params: { page, size, sortBy: 'tradingAmount', sortDirection: direction }
    });
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

export const getStocksSortedByYesterdayChangeRate = async (page = 0, size = 10, direction = 'DESC') => {
  try {
    const response = await axiosInstance.get('/sorted', {
      params: { page, size, sortBy: 'changeRate', sortDirection: direction }
    });
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

export const getTechnicalIndicator = async (stockCode, indicatorType) => {
  try {
    const response = await axiosInstance.get(`/${stockCode}/indicators/${indicatorType}`);
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};