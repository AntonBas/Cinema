import type { PersonResponse, PersonRequest, PersonRole, QuickCreatePersonRequest } from '@/types/person';
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

    search: (params?: SearchParams & { role?: PersonRole }): Promise<PageResponse<PersonResponse>> => {
      const url = buildPagedUrl(`${PUBLIC_URL}/search`, params, 'grid');
      return fetchApi<PageResponse<PersonResponse>>(url, {}, true);
    },

    getByRole: (role: PersonRole, params?: SearchParams): Promise<PageResponse<PersonResponse>> => {
      const url = buildPagedUrl(`${PUBLIC_URL}/role/${role}`, params, 'grid');
      return fetchApi<PageResponse<PersonResponse>>(url, {}, true);
    },
  },

  admin: {
    create: (request: PersonRequest): Promise<PersonResponse> =>
      fetchApi<PersonResponse>(ADMIN_URL, {
        method: 'POST',
        body: JSON.stringify(request),
      }),

    update: (id: number, request: PersonRequest): Promise<PersonResponse> =>
      fetchApi<PersonResponse>(`${ADMIN_URL}/${id}`, {
        method: 'PUT',
        body: JSON.stringify(request),
      }),

    delete: (id: number): Promise<void> =>
      fetchApi<void>(`${ADMIN_URL}/${id}`, {
        method: 'DELETE',
      }),

    quickCreate: (request: QuickCreatePersonRequest): Promise<PersonResponse> =>
      fetchApi<PersonResponse>(`${ADMIN_URL}/quick-create`, {
        method: 'POST',
        body: JSON.stringify(request),
      }),
  }
};