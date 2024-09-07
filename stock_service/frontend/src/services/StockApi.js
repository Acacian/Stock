import axios from 'axios';

const API_URL = process.env.REACT_APP_STOCK_API_URL || '/api/stock';
const AUTH_URL = process.env.REACT_APP_AUTH_URL || '/api/auth';

const axiosInstance = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

const refreshTokenRequest = async () => {
  const refreshToken = localStorage.getItem('refreshToken');
  if (!refreshToken) {
    throw new Error('No refresh token available');
  }
  try {
    const response = await axios.post(`${AUTH_URL}/refresh-token`, { refreshToken });
    const { token } = response.data;
    localStorage.setItem('token', token);
    return token;
  } catch (error) {
    console.error('Failed to refresh token:', error);
    throw error;
  }
};

const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  // 여기에 추가적인 로그아웃 로직을 구현할 수 있습니다.
  // 예: 리덕스 스토어 초기화, 로그인 페이지로 리다이렉트 등
  window.location.href = '/login'; // 로그인 페이지로 리다이렉트
};

axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response && error.response.status === 403 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        const newToken = await refreshTokenRequest();
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return axiosInstance(originalRequest);
      } catch (refreshError) {
        console.error('Token refresh failed:', refreshError);
        logout(); // 토큰 갱신 실패 시 자동 로그아웃
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);

const handleApiError = (error) => {
  if (error.response) {
    if (error.response.status === 403) {
      throw new Error('접근 권한이 없습니다. 로그인 상태를 확인해주세요.');
    }
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

const StockApi = {
  getAllStocks,
  getStockById,
  getStockPrices,
  searchStocks,
  getStocksSortedByYesterdayTrading,
  getStocksSortedByYesterdayChangeRate,
  getTechnicalIndicator,
};

export default StockApi;