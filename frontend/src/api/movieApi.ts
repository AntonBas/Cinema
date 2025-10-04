import type { MovieDto, MovieFormData } from '../types/Movie';
import { type GenreDto } from '../types/Genre';
import { type PersonDto } from '../types/Person';

const API_BASE_URL = 'http://localhost:8080/api';

const getAuthHeaders = () => {
  const token = localStorage.getItem('authToken');
  return {
    'Authorization': token ? `Bearer ${token}` : '',
    'Content-Type': 'application/json',
  };
};

export const movieApi = {
  getAll: async (): Promise<MovieDto[]> => {
    const response = await fetch(`${API_BASE_URL}/movies`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch movies');
    return response.json();
  },

  getById: async (id: number): Promise<MovieDto> => {
    const response = await fetch(`${API_BASE_URL}/movies/${id}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch movie');
    return response.json();
  },

  create: async (formData: FormData): Promise<MovieDto> => {
    const response = await fetch(`${API_BASE_URL}/movies`, {
      method: 'POST',
      headers: {
        'Authorization': localStorage.getItem('authToken') ? `Bearer ${localStorage.getItem('authToken')}` : '',
      },
      body: formData,
    });
    if (!response.ok) throw new Error('Failed to create movie');
    return response.json();
  },

  update: async (id: number, formData: FormData): Promise<MovieDto> => {
    const response = await fetch(`${API_BASE_URL}/movies/${id}`, {
      method: 'PUT',
      headers: {
        'Authorization': localStorage.getItem('authToken') ? `Bearer ${localStorage.getItem('authToken')}` : '',
      },
      body: formData,
    });
    if (!response.ok) throw new Error('Failed to update movie');
    return response.json();
  },

  delete: async (id: number): Promise<void> => {
    const response = await fetch(`${API_BASE_URL}/movies/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to delete movie');
  },

  getPoster: async (id: number): Promise<string> => {
    return `${API_BASE_URL}/movies/${id}/poster`;
  },

  getGenres: async (): Promise<GenreDto[]> => {
    const response = await fetch(`${API_BASE_URL}/genres`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch genres');
    return response.json();
  },

  getPersons: async (): Promise<PersonDto[]> => {
    const response = await fetch(`${API_BASE_URL}/persons`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch persons');
    return response.json();
  },
};