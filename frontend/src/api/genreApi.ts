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
    getById: (id: number): Promise<GenreResponse> =>
      fetchApi<GenreResponse>(`${PUBLIC_URL}/${id}`, {}, true),

    getAllPaginated: (params?: SearchParams): Promise<PageResponse<GenreResponse>> => {
      const url = buildPagedUrl(PUBLIC_URL, params, 'grid');
      return fetchApi<PageResponse<GenreResponse>>(url, {}, true);
    },

    search: (params?: SearchParams & { search?: string }): Promise<PageResponse<GenreResponse>> => {
      const url = buildPagedUrl(`${PUBLIC_URL}/search`, params, 'grid');
      return fetchApi<PageResponse<GenreResponse>>(url, {}, true);
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