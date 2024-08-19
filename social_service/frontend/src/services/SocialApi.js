const API_URL = '/api/social';

export const createPost = async (userId, content) => {
  const response = await fetch(`${API_URL}/posts`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, content })
  });
  return response.json();
};

export const addComment = async (userId, postId, content) => {
  const response = await fetch(`${API_URL}/posts/${postId}/comments`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, content })
  });
  return response.json();
};

export const likePost = async (userId, postId) => {
  await fetch(`${API_URL}/posts/${postId}/likes?userId=${userId}`, { method: 'POST' });
};

export const follow = async (followerId, followedId) => {
  await fetch(`${API_URL}/follow?followerId=${followerId}&followedId=${followedId}`, { method: 'POST' });
};

export const getPostsByUserId = async (userId) => {
  const response = await fetch(`${API_URL}/posts/user/${userId}`);
  return response.json();
};

export const getPostsWithActivity = async (userId) => {
  const response = await fetch(`${API_URL}/posts/${userId}/with-activity`);
  return response.json();
};