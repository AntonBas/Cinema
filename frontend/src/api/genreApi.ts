import type { GenreDto, GenreRequest } from '@/types/genre';
import type { PageResponse, SearchParams } from '@/types/pagination';

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
    const response = await fetch(`${API_URL}/all`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch all genres');
    return response.json();
  },


  getById: async (id: number): Promise<GenreDto> => {
    const response = await fetch(`${API_URL}/${id}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch genre');
    return response.json();
  },

  create: async (genreData: GenreRequest): Promise<GenreDto> => {
    const response = await fetch(API_URL, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(genreData),
    });
    if (!response.ok) throw new Error('Failed to create genre');
    return response.json();
  },

  update: async (id: number, genreData: GenreRequest): Promise<GenreDto> => {
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
  },

  search: async (params: SearchParams): Promise<PageResponse<GenreDto>> => {
    const { query, page = 0, size = 10 } = params;

    const searchParams = new URLSearchParams();
    if (query) searchParams.append('query', query);
    searchParams.append('page', page.toString());
    searchParams.append('size', size.toString());

    const response = await fetch(`${API_URL}?${searchParams}`, {
      headers: getAuthHeaders(),
    });

    if (!response.ok) throw new Error('Failed to search genres');
    return response.json();
  }
};