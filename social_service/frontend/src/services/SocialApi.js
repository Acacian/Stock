const API_URL = process.env.REACT_APP_SOCIAL_API_URL || 'https://localhost:8081/api/social';

const handleResponse = async (response) => {
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'An error occurred');
  }
  return response.json();
};

export const createPost = (userId, title, content) =>
  fetch(`${API_URL}/posts`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, title, content }),
    credentials: 'include',
  }).then(handleResponse);

export const addComment = (userId, postId, content) =>
  fetch(`${API_URL}/posts/${postId}/comments`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, content }),
    credentials: 'include',
  }).then(handleResponse);

export const likePost = (userId, postId) =>
  fetch(`${API_URL}/posts/${postId}/likes?userId=${userId}`, { 
    method: 'POST',
    credentials: 'include',
  }).then(handleResponse);

export const follow = (followerId, followedId) =>
  fetch(`${API_URL}/follow?followerId=${followerId}&followedId=${followedId}`, { 
    method: 'POST',
    credentials: 'include',
  }).then(handleResponse);

export const unfollow = (followerId, followedId) =>
  fetch(`${API_URL}/unfollow?followerId=${followerId}&followedId=${followedId}`, { 
    method: 'POST',
    credentials: 'include',
  }).then(handleResponse);

export const getPostsByUserId = (userId) =>
  fetch(`${API_URL}/posts/user/${userId}`, {
    credentials: 'include',
  }).then(handleResponse);

export const getPostsWithActivity = (userId) =>
  fetch(`${API_URL}/posts/${userId}/with-activity`, {
    credentials: 'include',
  }).then(handleResponse);

export const getFollowers = (userId) =>
  fetch(`${API_URL}/followers/${userId}`, {
    credentials: 'include',
  }).then(handleResponse);

export const getFollowing = (userId) =>
  fetch(`${API_URL}/following/${userId}`, {
    credentials: 'include',
  }).then(handleResponse);

export const searchPosts = (query, stockId, page = 0, size = 10, sortBy = 'createdAt', sortDirection = 'desc') => {
  let url = `${API_URL}/posts/search?query=${encodeURIComponent(query)}&page=${page}&size=${size}&sortBy=${sortBy}&sortDirection=${sortDirection}`;
  
  if (stockId) {
    url += `&stockId=${stockId}`;
  }

  return fetch(url, {
    credentials: 'include',
  }).then(handleResponse);
};