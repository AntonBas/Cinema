import type { PersonResponse, PersonRequest, QuickCreatePersonRequest } from '@/types/person';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { handleApiError } from '@/utils/apiErrorHandler';
import { buildPagedUrl } from '@/utils/paginationUtils';

const PUBLIC_URL = '/api/persons';
const ADMIN_URL = '/api/admin/persons';

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

export const personApi = {
  public: {
    getById: (id: number): Promise<PersonResponse> =>
      fetchApi<PersonResponse>(`${PUBLIC_URL}/${id}`, {}, true),
  },

  admin: {
    create: (request: PersonRequest): Promise<PersonResponse> =>
      fetchApi<PersonResponse>(ADMIN_URL, {
        method: 'POST',
        body: JSON.stringify(request),
      }),

    quickCreate: (request: QuickCreatePersonRequest): Promise<PersonResponse> =>
      fetchApi<PersonResponse>(`${ADMIN_URL}/quick`, {
        method: 'POST',
        body: JSON.stringify(request),
      }),

    getById: (id: number): Promise<PersonResponse> =>
      fetchApi<PersonResponse>(`${ADMIN_URL}/${id}`),

    update: (id: number, request: PersonRequest): Promise<PersonResponse> =>
      fetchApi<PersonResponse>(`${ADMIN_URL}/${id}`, {
        method: 'PUT',
        body: JSON.stringify(request),
      }),

    delete: (id: number): Promise<void> =>
      fetchApi<void>(`${ADMIN_URL}/${id}`, {
        method: 'DELETE',
      }),

    getAll: (params?: SearchParams & { name?: string; role?: string }): Promise<PageResponse<PersonResponse>> => {
      const url = buildPagedUrl(ADMIN_URL, params, 'table');
      return fetchApi<PageResponse<PersonResponse>>(url);
    },

    getActors: (params?: SearchParams & { name?: string }): Promise<PageResponse<PersonResponse>> => {
      const url = buildPagedUrl(`${ADMIN_URL}/actors`, params, 'table');
      return fetchApi<PageResponse<PersonResponse>>(url);
    },

    getDirectors: (params?: SearchParams & { name?: string }): Promise<PageResponse<PersonResponse>> => {
      const url = buildPagedUrl(`${ADMIN_URL}/directors`, params, 'table');
      return fetchApi<PageResponse<PersonResponse>>(url);
    },

    getScreenwriters: (params?: SearchParams & { name?: string }): Promise<PageResponse<PersonResponse>> => {
      const url = buildPagedUrl(`${ADMIN_URL}/screenwriters`, params, 'table');
      return fetchApi<PageResponse<PersonResponse>>(url);
    },

    getPopular: (params?: SearchParams & { name?: string; role?: string }): Promise<PageResponse<PersonResponse>> => {
      const url = buildPagedUrl(`${ADMIN_URL}/popular`, params, 'table');
      return fetchApi<PageResponse<PersonResponse>>(url);
    },
  }
};