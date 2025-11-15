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

const handleResponse = async (response: Response) => {
  if (!response.ok) {
    let errorMessage = `Request failed with status ${response.status}`;

    try {
      const errorData = await response.json();
      errorMessage = errorData.message || errorMessage;
    } catch {
    }

    throw new Error(errorMessage);
  }

  return response.json();
};

export const genreApi = {
  getAll: async (): Promise<GenreDto[]> => {
    const response = await fetch(`${API_URL}/all`, {
      headers: getAuthHeaders(),
    });
    return handleResponse(response);
  },

  getById: async (id: number): Promise<GenreDto> => {
    const response = await fetch(`${API_URL}/${id}`, {
      headers: getAuthHeaders(),
    });
    return handleResponse(response);
  },

  create: async (genreData: GenreRequest): Promise<GenreDto> => {
    const response = await fetch(API_URL, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(genreData),
    });
    return handleResponse(response);
  },

  update: async (id: number, genreData: GenreRequest): Promise<GenreDto> => {
    const response = await fetch(`${API_URL}/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(genreData),
    });
    return handleResponse(response);
  },

  delete: async (id: number): Promise<void> => {
    const response = await fetch(`${API_URL}/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      let errorMessage = `Request failed with status ${response.status}`;

      try {
        const errorData = await response.json();
        errorMessage = errorData.message || errorMessage;
      } catch {
      }

      throw new Error(errorMessage);
    }
  },

  search: async (params: SearchParams): Promise<PageResponse<GenreDto>> => {
    const { query, page, size = 12 } = params;

    const searchParams = new URLSearchParams();
    if (query) searchParams.append('query', query);
    searchParams.append('page', page?.toString() ?? '0');
    searchParams.append('size', size.toString());

    const response = await fetch(`${API_URL}?${searchParams}`, {
      headers: getAuthHeaders(),
    });

    return handleResponse(response);
  }
};