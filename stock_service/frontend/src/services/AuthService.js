import axios from 'axios';

const API_URL = process.env.REACT_APP_API_GATEWAY_URL || 'https://localhost:8081';

const axiosInstance = axios.create({
  baseURL: `${API_URL}/api/auth`,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
  timeout: 10000,
});

axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        const refreshToken = localStorage.getItem('refreshToken');
        const response = await refreshToken(refreshToken);
        localStorage.setItem('accessToken', response.accessToken);
        localStorage.setItem('refreshToken', response.refreshToken);
        axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${response.accessToken}`;
        return axiosInstance(originalRequest);
      } catch (refreshError) {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login';
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
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

export const logoutUser = async () => {
  try {
    await axiosInstance.post('/logout');
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

export const refreshToken = async (refreshToken) => {
  try {
    const response = await axiosInstance.post('/refresh-token', { refreshToken });
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

export const decodeJWT = (token) => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(jsonPayload);
  } catch (error) {
    console.error('Error decoding JWT:', error);
    return null;
  }
};