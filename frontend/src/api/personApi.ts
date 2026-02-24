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
  }
};

export const personKeys = {
  all: ['persons'] as const,
  admin: {
    all: ['persons', 'admin'] as const,
    lists: () => [...personKeys.admin.all, 'list'] as const,
    list: (params?: SearchParams & { name?: string; role?: string }) =>
      [...personKeys.admin.lists(), params] as const,
    details: () => [...personKeys.admin.all, 'detail'] as const,
    detail: (id: number) => [...personKeys.admin.details(), id] as const,
  },
  public: {
    all: ['persons', 'public'] as const,
    detail: (id: number) => [...personKeys.public.all, id] as const,
  }
};