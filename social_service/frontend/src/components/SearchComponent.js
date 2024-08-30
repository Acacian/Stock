import React, { useState } from 'react';
import { searchPosts } from '../services/SocialApi';

const SearchComponent = () => {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);

  const handleSearch = async (e) => {
    e.preventDefault();
    try {
      const searchResults = await searchPosts(query);
      setResults(searchResults.content); // 페이지 객체에서 content를 가져옵니다.
    } catch (error) {
      console.error('게시물 검색 중 오류 발생:', error);
    }
  };

  return (
    <div>
      <form onSubmit={handleSearch}>
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="게시물 검색..."
        />
        <button type="submit">검색</button>
      </form>
      <div>
        {results.map(post => (
          <div key={post.id}>
            <h3>{post.title}</h3>
            <p>{post.content}</p>
          </div>
        ))}
      </div>
    </div>
  );
};

export default SearchComponent;