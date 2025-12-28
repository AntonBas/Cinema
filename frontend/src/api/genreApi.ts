import type { GenreResponse, GenreRequest } from '@/types/genre';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { handleApiError } from '@/utils/apiErrorHandler';

const PUBLIC_API_URL = '/api/genres';
const ADMIN_API_URL = '/api/admin/genres';

const getAuthHeaders = () => {
  const token = localStorage.getItem('authToken');
  return {
    'Content-Type': 'application/json',
    'Authorization': token ? `Bearer ${token}` : '',
  };
};

export const genreApi = {
  getAll: async (): Promise<GenreResponse[]> => {
    const response = await fetch(`${PUBLIC_API_URL}/all`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  getById: async (id: number): Promise<GenreResponse> => {
    const response = await fetch(`${PUBLIC_API_URL}/${id}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  search: async (params: SearchParams): Promise<PageResponse<GenreResponse>> => {
    const { query, page, size = 12 } = params;

    const searchParams = new URLSearchParams();
    if (query) searchParams.append('query', query);
    searchParams.append('page', page?.toString() ?? '0');
    searchParams.append('size', size.toString());

    const response = await fetch(`${PUBLIC_API_URL}?${searchParams}`, {
      headers: getAuthHeaders(),
    });

    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  create: async (genreData: GenreRequest): Promise<GenreResponse> => {
    const response = await fetch(ADMIN_API_URL, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(genreData),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  update: async (id: number, genreData: GenreRequest): Promise<GenreResponse> => {
    const response = await fetch(`${ADMIN_API_URL}/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(genreData),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  delete: async (id: number): Promise<void> => {
    const response = await fetch(`${ADMIN_API_URL}/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw await handleApiError(response);
  },
};