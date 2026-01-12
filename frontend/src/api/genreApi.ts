import type { GenreResponse, GenreRequest } from '@/types/genre';
import type { PageResponse } from '@/types/pagination';
import { handleApiError } from '@/utils/apiErrorHandler';

const PUBLIC_URL = '/api/genres';
const ADMIN_URL = '/api/admin/genres';

const getAuthHeaders = (): HeadersInit => {
  const token = localStorage.getItem('authToken');
  return {
    'Content-Type': 'application/json',
    ...(token && { 'Authorization': `Bearer ${token}` }),
  };
};

const getPublicHeaders = (): HeadersInit => {
  return {
    'Content-Type': 'application/json',
  };
};

const fetchApi = async <T>(url: string, options: RequestInit = {}, isPublic: boolean = false): Promise<T> => {
  const headers = isPublic ? getPublicHeaders() : getAuthHeaders();

  const response = await fetch(url, {
    headers,
    ...options,
  });

  if (!response.ok) throw await handleApiError(response);
  if (response.status === 204) return undefined as T;

  return response.json();
};

export const genreApi = {
  public: {
    getById: (id: number): Promise<GenreResponse> =>
      fetchApi<GenreResponse>(`${PUBLIC_URL}/${id}`, {}, true),

    getAllPaginated: (page?: number, size: number = 12, sort: string = 'name'): Promise<PageResponse<GenreResponse>> => {
      const params = new URLSearchParams();
      if (page !== undefined) params.append('page', page.toString());
      params.append('size', size.toString());
      params.append('sort', sort);

      return fetchApi<PageResponse<GenreResponse>>(`${PUBLIC_URL}?${params}`, {}, true);
    },

    search: (query?: string, page?: number, size: number = 12): Promise<PageResponse<GenreResponse>> => {
      const params = new URLSearchParams();
      if (query) params.append('query', query);
      if (page !== undefined) params.append('page', page.toString());
      params.append('size', size.toString());
      params.append('sort', 'name');

      return fetchApi<PageResponse<GenreResponse>>(`${PUBLIC_URL}/search?${params}`, {}, true);
    },

    getAll: (): Promise<GenreResponse[]> =>
      fetchApi<GenreResponse[]>(`${PUBLIC_URL}/all`, {}, true),

    getForSelect: (): Promise<GenreResponse[]> =>
      fetchApi<GenreResponse[]>(`${PUBLIC_URL}/select`, {}, true),
  },

  admin: {
    create: (request: GenreRequest): Promise<GenreResponse> =>
      fetchApi<GenreResponse>(ADMIN_URL, {
        method: 'POST',
        body: JSON.stringify(request),
      }),

    getById: (id: number): Promise<GenreResponse> =>
      fetchApi<GenreResponse>(`${ADMIN_URL}/${id}`),

    update: (id: number, request: GenreRequest): Promise<GenreResponse> =>
      fetchApi<GenreResponse>(`${ADMIN_URL}/${id}`, {
        method: 'PUT',
        body: JSON.stringify(request),
      }),

    delete: (id: number): Promise<void> =>
      fetchApi<void>(`${ADMIN_URL}/${id}`, {
        method: 'DELETE',
      }),
  }
};