const accessTokenField = document.getElementById('access-token');
const newsfeedOutput = document.getElementById('newsfeed-output');
const stocksOutput = document.getElementById('stocks-output');
const logContainer = document.getElementById('request-log');

function getToken() {
  return accessTokenField.value.trim();
}

function setToken(token) {
  accessTokenField.value = token || '';
}

function formatJson(value) {
  return JSON.stringify(value, null, 2);
}

function appendLog(title, payload) {
  const entry = document.createElement('div');
  entry.className = 'log-entry';

  const meta = document.createElement('div');
  meta.className = 'log-meta';
  meta.textContent = `${new Date().toLocaleTimeString()} | ${title}`;

  const pre = document.createElement('pre');
  pre.textContent = typeof payload === 'string' ? payload : formatJson(payload);

  entry.append(meta, pre);
  logContainer.prepend(entry);
}

async function apiRequest(path, { method = 'GET', body, auth = false } = {}) {
  const headers = { 'Content-Type': 'application/json' };

  if (auth && getToken()) {
    headers.Authorization = `Bearer ${getToken()}`;
  }

  const response = await fetch(path, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
    credentials: 'include',
  });

  const raw = await response.text();
  let data = raw;

  try {
    data = raw ? JSON.parse(raw) : {};
  } catch (error) {
    data = raw;
  }

  if (!response.ok) {
    throw new Error(typeof data === 'string' ? data : formatJson(data));
  }

  return data;
}

document.getElementById('login-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const form = new FormData(event.currentTarget);

  try {
    const data = await apiRequest('/api/auth/login', {
      method: 'POST',
      body: {
        email: form.get('email'),
        password: form.get('password'),
      },
    });

    setToken(data.accessToken || '');
    appendLog('LOGIN', data);
  } catch (error) {
    appendLog('LOGIN ERROR', error.message);
  }
});

document.getElementById('register-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const form = new FormData(event.currentTarget);

  try {
    const data = await apiRequest('/api/auth/register', {
      method: 'POST',
      body: {
        name: form.get('name'),
        email: form.get('email'),
        password: form.get('password'),
      },
    });

    appendLog('REGISTER', data);
  } catch (error) {
    appendLog('REGISTER ERROR', error.message);
  }
});

document.getElementById('post-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const form = new FormData(event.currentTarget);

  try {
    const data = await apiRequest('/api/social/posts', {
      method: 'POST',
      auth: true,
      body: {
        userId: Number(form.get('userId')),
        title: form.get('title'),
        content: form.get('content'),
        stockId: form.get('stockId') ? Number(form.get('stockId')) : null,
      },
    });

    appendLog('CREATE POST', data);
  } catch (error) {
    appendLog('CREATE POST ERROR', error.message);
  }
});

document.getElementById('follow-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const form = new FormData(event.currentTarget);
  const followerId = Number(form.get('followerId'));
  const followeeId = Number(form.get('followeeId'));

  try {
    const data = await apiRequest(`/api/social/follow?followerId=${followerId}&followeeId=${followeeId}`, {
      method: 'POST',
      auth: true,
    });

    appendLog('FOLLOW', data || { followerId, followeeId, status: 'OK' });
  } catch (error) {
    appendLog('FOLLOW ERROR', error.message);
  }
});

document.getElementById('comment-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const form = new FormData(event.currentTarget);
  const postId = Number(form.get('postId'));

  try {
    const data = await apiRequest(`/api/social/posts/${postId}/comments`, {
      method: 'POST',
      auth: true,
      body: {
        userId: Number(form.get('userId')),
        content: form.get('content'),
      },
    });

    appendLog('ADD COMMENT', data);
  } catch (error) {
    appendLog('ADD COMMENT ERROR', error.message);
  }
});

document.getElementById('like-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const form = new FormData(event.currentTarget);
  const postId = Number(form.get('postId'));
  const userId = Number(form.get('userId'));

  try {
    const data = await apiRequest(`/api/social/posts/${postId}/like?userId=${userId}`, {
      method: 'POST',
      auth: true,
    });

    appendLog('LIKE POST', data || { postId, userId, status: 'OK' });
  } catch (error) {
    appendLog('LIKE POST ERROR', error.message);
  }
});

document.getElementById('newsfeed-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const form = new FormData(event.currentTarget);
  const userId = Number(form.get('userId'));

  try {
    const data = await apiRequest(`/api/newsfeed/${userId}`, { auth: true });
    newsfeedOutput.textContent = formatJson(data);
    appendLog('GET NEWSFEED', data);
  } catch (error) {
    appendLog('GET NEWSFEED ERROR', error.message);
  }
});

document.getElementById('stocks-button').addEventListener('click', async () => {
  try {
    const data = await apiRequest('/api/stocks?page=0&size=5', { auth: true });
    stocksOutput.textContent = formatJson(data);
    appendLog('GET STOCKS', data);
  } catch (error) {
    appendLog('GET STOCKS ERROR', error.message);
  }
});

document.getElementById('clear-log').addEventListener('click', () => {
  logContainer.innerHTML = '';
});

appendLog('READY', {
  message: '게이트웨이를 통해 auth/social/newsfeed/stock API를 점검할 수 있습니다.',
});
