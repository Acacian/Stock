import axios from 'axios';
import TokenService from './TokenService';

const API_URL = process.env.REACT_APP_STOCK_API_URL || '/api/stock';
const AUTH_URL = process.env.REACT_APP_AUTH_URL || 'https://localhost:3001/api/auth';

const axiosInstance = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 인증이 필요한 요청에만 토큰을 추가하는 함수
const addAuthHeader = (config) => {
  const token = TokenService.getAccessToken();
  if (token) {
    config.headers['Authorization'] = 'Bearer ' + token;
  }
  return config;
};

// 인터셉터 수정: 댓글 관련 API에만 인증 헤더 추가
axiosInstance.interceptors.request.use(
  (config) => {
    if (config.url.includes('/comments')) {
      return addAuthHeader(config);
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터는 그대로 유지 (토큰 갱신 로직)
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        const refreshToken = TokenService.getRefreshToken();
        const response = await axios.post(`${AUTH_URL}/refresh`, { refreshToken: refreshToken });
        const { accessToken, refreshToken: newRefreshToken } = response.data;
        TokenService.setTokens(accessToken, newRefreshToken || refreshToken);
        originalRequest.headers['Authorization'] = 'Bearer ' + accessToken;
        return axiosInstance(originalRequest);
      } catch (refreshError) {
        console.error('Token refresh failed:', refreshError);
        TokenService.removeTokens();
        window.location.href = AUTH_URL;
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);

export const getAllStocks = async (page = 0, size = 10, sort = 'name,asc') => {
  try {
    const response = await axiosInstance.get('', { params: { page, size, sort } });
    return response.data;
  } catch (error) {
    throw new Error(error.response?.data?.message || '주식 목록을 불러오는데 실패했습니다.');
  }
};

export const getStockById = async (id) => {
  try {
    const response = await axiosInstance.get(`/${id}`);
    return response.data;
  } catch (error) {
    throw new Error(error.response?.data?.message || '주식 정보를 불러오는데 실패했습니다.');
  }
};

export const getStockPrices = async (stockId, period = 'daily', startDate, endDate) => {
  try {
    const response = await axiosInstance.get(`/${stockId}/prices/${period}`, {
      params: { startDate, endDate }
    });
    return response.data;
  } catch (error) {
    throw new Error(error.response?.data?.message || '주식 가격 정보를 불러오는데 실패했습니다.');
  }
};

export const searchStocks = async (query, page = 0, size = 10, sort = 'name,asc') => {
  try {
    const response = await axiosInstance.get('/search', {
      params: { query, page, size, sort }
    });
    return response.data;
  } catch (error) {
    throw new Error(error.response?.data?.message || '주식 검색에 실패했습니다.');
  }
};

export const getStocksSortedByYesterdayTrading = async (page = 0, size = 10, direction = 'DESC') => {
  try {
    const response = await axiosInstance.get('/sorted', {
      params: { page, size, sortBy: 'tradingAmount', sortDirection: direction }
    });
    return response.data;
  } catch (error) {
    throw new Error(error.response?.data?.message || '주식 정렬에 실패했습니다.');
  }
};

export const getStocksSortedByYesterdayChangeRate = async (page = 0, size = 10, direction = 'DESC') => {
  try {
    const response = await axiosInstance.get('/sorted', {
      params: { page, size, sortBy: 'changeRate', sortDirection: direction }
    });
    return response.data;
  } catch (error) {
    throw new Error(error.response?.data?.message || '주식 정렬에 실패했습니다.');
  }
};

export const getTechnicalIndicator = async (stockCode, indicatorType) => {
  try {
    const response = await axiosInstance.get(`/${stockCode}/indicators/${indicatorType}`);
    return response.data;
  } catch (error) {
    throw new Error(error.response?.data?.message || '기술적 지표를 불러오는데 실패했습니다.');
  }
};

export const getComments = async (stockId, page = 0, size = 10) => {
  try {
    const response = await axiosInstance.get(`/${stockId}/comments`, { 
      params: { page, size },
      headers: addAuthHeader({}).headers // 인증 헤더 추가
    });
    return response.data;
  } catch (error) {
    throw new Error(error.response?.data?.message || '댓글을 불러오는데 실패했습니다.');
  }
};

export const addComment = async (stockId, content) => {
  try {
    const response = await axiosInstance.post(`/${stockId}/comments`, { content }, {
      headers: addAuthHeader({}).headers // 인증 헤더 추가
    });
    return response.data;
  } catch (error) {
    throw new Error(error.response?.data?.message || '댓글 작성에 실패했습니다.');
  }
};

const StockApi = {
  getAllStocks,
  getStockById,
  getStockPrices,
  searchStocks,
  getStocksSortedByYesterdayTrading,
  getStocksSortedByYesterdayChangeRate,
  getTechnicalIndicator,
  getComments,
  addComment,
};

export default StockApi;