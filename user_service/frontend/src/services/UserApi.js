import axios from 'axios';

const API_URL = process.env.REACT_APP_AUTH_URL;

const axiosInstance = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
  timeout: 10000,
});

axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token');
      window.location.reload(true);
    }
    return Promise.reject(error);
  }
);

const handleApiError = (error) => {
  if (error.response) {
    // 서버에서 응답을 받은 경우
    throw new Error(error.response.data.message || '서버 오류가 발생했습니다.');
  } else if (error.request) {
    // 요청이 전송되었지만 응답을 받지 못한 경우
    throw new Error('서버에 연결할 수 없습니다. 네트워크 연결을 확인해 주세요.');
  } else {
    // 요청 설정 중 오류가 발생한 경우
    throw new Error('예기치 않은 오류가 발생했습니다.');
  }
};

export const registerUser = async (userData) => {
  try {
    const response = await axiosInstance.post('/register', userData);
    console.log('Request URL:', axiosInstance.getUri() + '/register');
    return response.data;
  } catch (error) {
    console.error('Registration error:', error);
    handleApiError(error);
  }
};

export const loginUser = async (email, password) => {
  try {
    const response = await axiosInstance.post('/login', { email, password });
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
    }
    return response.data;
  } catch (error) {
    console.error('Login error:', error);
    throw error;
  }
};

export const logoutUser = async () => {
  try {
    await axiosInstance.post('/logout');
    localStorage.removeItem('token');
  } catch (error) {
    handleApiError(error);
  }
};

export const getUserProfile = async (userId) => {
  try {
    const response = await axiosInstance.get(`/users/${userId}`);
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

export const updateUserProfile = async (userId, profileData) => {
  try {
    const response = await axiosInstance.put(`/users/${userId}`, profileData);
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

export const updatePassword = async (userId, oldPassword, newPassword) => {
  try {
    const response = await axiosInstance.put('/password', { userId, oldPassword, newPassword });
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

export const checkAuthStatus = async () => {
  try {
    const response = await axiosInstance.get('/check');
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};