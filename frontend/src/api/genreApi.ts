import type { GenreDto, GenreFormData } from '../types/Genre';

const API_URL = 'http://localhost:8080/api/genres';

const getAuthHeaders = () => {
  const token = localStorage.getItem('authToken');
  return {
    'Content-Type': 'application/json',
    'Authorization': token ? `Bearer ${token}` : '',
  };
};

export const genreApi = {
  getAll: async (): Promise<GenreDto[]> => {
    const response = await fetch(API_URL, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch genres');
    return response.json();
  },

  getById: async (id: number): Promise<GenreDto> => {
    const response = await fetch(`${API_URL}/${id}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch genre');
    return response.json();
  },

  create: async (genreData: GenreFormData): Promise<GenreDto> => {
    const response = await fetch(API_URL, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(genreData),
    });
    if (!response.ok) throw new Error('Failed to create genre');
    return response.json();
  },

  update: async (id: number, genreData: GenreFormData): Promise<GenreDto> => {
    const response = await fetch(`${API_URL}/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(genreData),
    });
    if (!response.ok) throw new Error('Failed to update genre');
    return response.json();
  },

  delete: async (id: number): Promise<void> => {
    const response = await fetch(`${API_URL}/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to delete genre');
  }
};