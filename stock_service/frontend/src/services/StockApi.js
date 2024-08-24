// src/services/stockapi.js
import axios from 'axios';

const API_URL = '/api/stocks';

export const getAllStocks = async (page = 0, size = 10, sort = 'name,asc') => {
  try {
    const response = await axios.get(`${API_URL}`, { params: { page, size, sort } });
    return response.data;
  } catch (error) {
    console.error('Error fetching stocks:', error);
    throw error;
  }
};

export const getStockById = async (id) => {
  try {
    const response = await axios.get(`${API_URL}/${id}`);
    return response.data;
  } catch (error) {
    console.error('Error fetching stock details:', error);
    throw error;
  }
};

export const getStockPrices = async (stockId, period = 'daily', startDate, endDate) => {
  try {
    const response = await axios.get(`${API_URL}/${stockId}/prices/${period}`, {
      params: { startDate, endDate }
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching stock prices:', error);
    throw error;
  }
};

export const searchStocks = async (query, page = 0, size = 10) => {
  try {
    const response = await axios.get(`${API_URL}/search`, {
      params: { query, page, size }
    });
    return response.data;
  } catch (error) {
    console.error('Error searching stocks:', error);
    throw error;
  }
};