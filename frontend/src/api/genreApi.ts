import type { GenreResponse, GenreRequest, GenreStatsResponse } from '@/types/genre';
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

    getPopular: (query?: string, limit: number = 10): Promise<GenreResponse[]> => {
      const url = new URL(`${PUBLIC_URL}/popular`, window.location.origin);
      if (query) url.searchParams.append('query', query);
      url.searchParams.append('limit', limit.toString());
      return fetchApi<GenreResponse[]>(url.toString(), {}, true);
    },

    getByIds: (ids: number[]): Promise<GenreResponse[]> => {
      const url = new URL(`${PUBLIC_URL}/by-ids`, window.location.origin);
      ids.forEach(id => url.searchParams.append('ids', id.toString()));
      return fetchApi<GenreResponse[]>(url.toString(), {}, true);
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

    getAllWithStats: (params?: SearchParams): Promise<PageResponse<GenreStatsResponse>> => {
      const apiParams: SearchParams = { ...params };

      if (params?.search !== undefined) {
        apiParams.search = params.search;
      }

      const url = buildPagedUrl(ADMIN_URL, apiParams, 'admin');
      return fetchApi<PageResponse<GenreStatsResponse>>(url);
    },
  }
};