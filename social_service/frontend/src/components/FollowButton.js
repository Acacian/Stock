import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';

const SearchComponent = () => {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const { socialActions } = useAuth();

  const handleSearch = async (e) => {
    e.preventDefault();
    try {
      const searchResults = await socialActions.searchPosts(query);
      setResults(searchResults.content);
    } catch (error) {
      console.error('Error searching posts:', error);
    }
  };

  return (
    <div>
      <form onSubmit={handleSearch}>
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Search posts..."
        />
        <button type="submit">Search</button>
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