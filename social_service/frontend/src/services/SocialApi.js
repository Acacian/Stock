const API_URL = process.env.REACT_APP_SOCIAL_API_URL || '/api/social';

const handleResponse = async (response) => {
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'An error occurred');
  }
  return response.json();
};

export const createPost = (userId, content) =>
  fetch(`${API_URL}/posts`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, content })
  }).then(handleResponse);

export const addComment = (userId, postId, content) =>
  fetch(`${API_URL}/posts/${postId}/comments`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, content })
  }).then(handleResponse);

export const likePost = (userId, postId) =>
  fetch(`${API_URL}/posts/${postId}/likes?userId=${userId}`, { method: 'POST' }).then(handleResponse);

export const follow = (followerId, followedId) =>
  fetch(`${API_URL}/follow?followerId=${followerId}&followedId=${followedId}`, { method: 'POST' }).then(handleResponse);

export const unfollow = (followerId, followedId) =>
  fetch(`${API_URL}/unfollow?followerId=${followerId}&followedId=${followedId}`, { method: 'POST' }).then(handleResponse);

export const getPostsByUserId = (userId) =>
  fetch(`${API_URL}/posts/user/${userId}`).then(handleResponse);

export const getPostsWithActivity = (userId) =>
  fetch(`${API_URL}/posts/${userId}/with-activity`).then(handleResponse);

export const getFollowers = (userId) =>
  fetch(`${API_URL}/followers/${userId}`).then(handleResponse);

export const getFollowing = (userId) =>
  fetch(`${API_URL}/following/${userId}`).then(handleResponse);