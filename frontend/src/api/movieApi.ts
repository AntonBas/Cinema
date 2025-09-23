import type { MovieDto, MovieFormData } from '../types/Movie';

const API_URL = 'http://localhost:8080/api/movies';

const getAuthHeaders = () => {
  const token = localStorage.getItem('authToken');
  return {
    'Authorization': token ? `Bearer ${token}` : '',
  };
};

export const movieApi = {
  getAll: async (): Promise<MovieDto[]> => {
    const response = await fetch(API_URL, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch movies');
    return response.json();
  },

  getById: async (id: number): Promise<MovieDto> => {
    const response = await fetch(`${API_URL}/${id}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch movie');
    return response.json();
  },

  getByStatus: async (status: string): Promise<MovieDto[]> => {
    const response = await fetch(`${API_URL}/status/${status}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch movies by status');
    return response.json();
  },

  getCurrentlyShowing: async (): Promise<MovieDto[]> => {
    const response = await fetch(`${API_URL}/current`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch current movies');
    return response.json();
  },

  getUpcoming: async (): Promise<MovieDto[]> => {
    const response = await fetch(`${API_URL}/upcoming`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch upcoming movies');
    return response.json();
  },

  create: async (movieData: MovieFormData): Promise<MovieDto> => {
    const response = await fetch(API_URL, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(movieData),
    });
    if (!response.ok) throw new Error('Failed to create movie');
    return response.json();
  },

  update: async (id: number, movieData: MovieFormData): Promise<MovieDto> => {
    const response = await fetch(`${API_URL}/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(movieData),
    });
    if (!response.ok) throw new Error('Failed to update movie');
    return response.json();
  },

  delete: async (id: number): Promise<void> => {
    const response = await fetch(`${API_URL}/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to delete movie');
  }
};