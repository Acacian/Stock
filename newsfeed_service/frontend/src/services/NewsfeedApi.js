import axios from 'axios';
import TokenService from './TokenService';

const API_URL = process.env.REACT_APP_NEWSFEED_API_URL || 'https://localhost:8081/api/newsfeed';
const AUTH_URL = process.env.REACT_APP_AUTH_URL || 'https://localhost:3001/api/auth';

const axiosInstance = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

axiosInstance.interceptors.request.use((config) => {
  const token = TokenService.getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response && error.response.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        const refreshToken = TokenService.getRefreshToken();
        const response = await axios.post(`${AUTH_URL}/refresh`, { refreshToken });
        const { accessToken, refreshToken: newRefreshToken } = response.data;
        TokenService.setTokens(accessToken, newRefreshToken);
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return axiosInstance(originalRequest);
      } catch (refreshError) {
        console.error('Token refresh failed:', refreshError);
        TokenService.removeTokens();
        window.location.href = AUTH_URL; // 인증 서비스 URL로 리다이렉트
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);

const handleApiError = (error) => {
  if (error.response) {
    throw new Error(error.response.data.message || '서버 오류가 발생했습니다.');
  } else if (error.request) {
    throw new Error('서버에 연결할 수 없습니다. 네트워크 연결을 확인해 주세요.');
  } else {
    throw new Error('예기치 않은 오류가 발생했습니다.');
  }
};

const fetchNewsfeed = async () => {
  try {
    const response = await axiosInstance.get('/');
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

const NewsfeedApi = {
  fetchNewsfeed,
};

export default NewsfeedApi;