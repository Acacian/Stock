import axios from 'axios';
import TokenService from './TokenService';

const API_URL = process.env.REACT_APP_AUTH_URL || 'https://localhost:3001/api/auth';

const axiosInstance = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
  timeout: 10000,
});

axiosInstance.interceptors.request.use((config) => {
  const token = TokenService.getAccessToken();
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`;
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response && error.response.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        const refreshToken = TokenService.getRefreshToken();
        const response = await refreshTokenApi(refreshToken);
        TokenService.setTokens(response.accessToken, response.refreshToken);
        originalRequest.headers['Authorization'] = `Bearer ${response.accessToken}`;
        return axiosInstance(originalRequest);
      } catch (refreshError) {
        console.error('Token refresh failed:', refreshError);
        TokenService.removeTokens();
        window.location.href = API_URL; // 인증 서비스 URL로 리다이렉트
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);

const handleApiError = (error) => {
  console.error('API Error:', error);
  if (error.response) {
    throw new Error(error.response.data.message || `Error: ${error.response.status}`);
  } else if (error.request) {
    throw new Error('No response received from server');
  } else {
    throw new Error('Error setting up request');
  }
};

export const registerUser = async (userData) => {
  try {
    const response = await axiosInstance.post('/register', userData);
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

export const loginUser = async (email, password) => {
  try {
    const response = await axiosInstance.post('/login', { email, password });
    if (response.data.accessToken && response.data.refreshToken) {
      TokenService.setTokens(response.data.accessToken, response.data.refreshToken);
    }
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

export const logoutUser = async () => {
  try {
    await axiosInstance.post('/logout');
    TokenService.removeTokens();
  } catch (error) {
    handleApiError(error);
  }
};

export const getUserProfile = async () => {
  try {
    const response = await axiosInstance.get('/users/me');
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

export const updateUserProfile = async (profileData) => {
  try {
    const response = await axiosInstance.put('/users/me', profileData);
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

export const updatePassword = async (oldPassword, newPassword) => {
  try {
    const response = await axiosInstance.put('/password', { oldPassword, newPassword });
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

export const uploadProfileImage = async (formData) => {
  try {
    const response = await axiosInstance.post('/profile/uploadImage', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

const refreshTokenApi = async (refreshToken) => {
  try {
    const response = await axiosInstance.post('/refresh', { refreshToken });
    return response.data;
  } catch (error) {
    console.error('Refresh token request failed:', error);
    throw error;
  }
};

const UserApi = {
  registerUser,
  loginUser,
  logoutUser,
  getUserProfile,
  updateUserProfile,
  updatePassword,
  uploadProfileImage,
  refreshToken: refreshTokenApi,
};

export default UserApi;