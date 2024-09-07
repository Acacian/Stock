import axios from 'axios';

const API_URL = process.env.REACT_APP_SOCIAL_API_URL || 'https://localhost:8081/api/social';
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
  window.location.href = '/login';
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
        logout();
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

export const createPost = async (userId, title, content) => {
  try {
    const response = await axiosInstance.post('/posts', { userId, title, content });
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

export const addComment = async (userId, postId, content) => {
  try {
    const response = await axiosInstance.post(`/posts/${postId}/comments`, { userId, content });
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

export const likePost = async (userId, postId) => {
  try {
    const response = await axiosInstance.post(`/posts/${postId}/likes`, { userId });
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

export const follow = async (followerId, followedId) => {
  try {
    const response = await axiosInstance.post('/follow', { followerId, followedId });
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

export const unfollow = async (followerId, followedId) => {
  try {
    const response = await axiosInstance.post('/unfollow', { followerId, followedId });
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

export const getPostsByUserId = async (userId) => {
  try {
    const response = await axiosInstance.get(`/posts/user/${userId}`);
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

export const getPostsWithActivity = async (userId) => {
  try {
    const response = await axiosInstance.get(`/posts/${userId}/with-activity`);
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

export const getFollowers = async (userId) => {
  try {
    const response = await axiosInstance.get(`/followers/${userId}`);
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

export const getFollowing = async (userId) => {
  try {
    const response = await axiosInstance.get(`/following/${userId}`);
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};

export const searchPosts = async (query, stockId, page = 0, size = 10, sortBy = 'createdAt', sortDirection = 'desc') => {
  try {
    const params = { query, page, size, sortBy, sortDirection };
    if (stockId) params.stockId = stockId;
    const response = await axiosInstance.get('/posts/search', { params });
    return response.data;
  } catch (error) {
    handleApiError(error);
  }
};