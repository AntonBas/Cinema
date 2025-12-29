import type { PersonResponse, PersonRequest, PersonRole, QuickCreatePersonRequest } from '@/types/person';
import type { PageResponse } from '@/types/pagination';
import { handleApiError } from '@/utils/apiErrorHandler';

const PUBLIC_API_URL = '/api/persons';
const ADMIN_API_URL = '/api/admin/persons';

const getAuthHeaders = () => {
  const token = localStorage.getItem('authToken');
  return {
    'Content-Type': 'application/json',
    'Authorization': token ? `Bearer ${token}` : '',
  };
};

const buildQueryParams = (params: Record<string, any>): string => {
  const searchParams = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      searchParams.append(key, String(value));
    }
  });

  return searchParams.toString();
};

export const personApi = {
  getById: async (id: number): Promise<PersonResponse> => {
    const response = await fetch(`${PUBLIC_API_URL}/${id}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  getAllPaginated: async (page: number = 0, size: number = 12): Promise<PageResponse<PersonResponse>> => {
    const searchParams = buildQueryParams({ page, size });
    const response = await fetch(`${PUBLIC_API_URL}/search?${searchParams}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  getAll: async (): Promise<PersonResponse[]> => {
    let allPersons: PersonResponse[] = [];
    let currentPage = 0;
    const pageSize = 100;

    try {
      while (true) {
        const response = await personApi.getAllPaginated(currentPage, pageSize);
        allPersons = [...allPersons, ...response.content];

        if (response.last) break;
        currentPage++;
      }
      return allPersons;
    } catch (error) {
      console.error('Error fetching all persons:', error);
      throw error;
    }
  },

  search: async (params: {
    query?: string;
    role?: PersonRole;
    page?: number;
    size?: number;
  }): Promise<PageResponse<PersonResponse>> => {
    const { query, role, page = 0, size = 12 } = params;
    const searchParams = buildQueryParams({ query, role, page, size });
    const response = await fetch(`${PUBLIC_API_URL}/search?${searchParams}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  getByRole: async (role: PersonRole, page: number = 0, size: number = 12): Promise<PageResponse<PersonResponse>> => {
    const searchParams = buildQueryParams({ page, size });
    const response = await fetch(`${PUBLIC_API_URL}/role/${role}?${searchParams}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  create: async (personData: PersonRequest): Promise<PersonResponse> => {
    const response = await fetch(ADMIN_API_URL, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(personData),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  quickCreate: async (personData: QuickCreatePersonRequest): Promise<PersonResponse> => {
    const response = await fetch(`${ADMIN_API_URL}/quick-create`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(personData),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  update: async (id: number, personData: PersonRequest): Promise<PersonResponse> => {
    const response = await fetch(`${ADMIN_API_URL}/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(personData),
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

  getByIdAdmin: async (id: number): Promise<PersonResponse> => {
    const response = await fetch(`${ADMIN_API_URL}/${id}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },
};