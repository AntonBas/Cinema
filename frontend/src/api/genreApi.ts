import type { GenreResponse, GenreRequest } from '@/types/genre';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { handleApiError } from '@/utils/apiErrorHandler';
import { buildPagedUrl } from '@/utils/paginationUtils';

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
    search: (query: string, limit: number = 10): Promise<GenreResponse[]> => {
      const url = `${PUBLIC_URL}/search?query=${encodeURIComponent(query)}&limit=${limit}`;
      return fetchApi<GenreResponse[]>(url, {}, true);
    },
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

    getAll: (params?: SearchParams): Promise<PageResponse<GenreResponse>> => {
      const url = buildPagedUrl(ADMIN_URL, params, 'admin');
      return fetchApi<PageResponse<GenreResponse>>(url);
    },
  }
};