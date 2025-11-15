import type { PersonResponse, PersonRequest, PersonRole, QuickCreatePersonRequest } from '@/types/person';
import type { PageResponse } from '@/types/pagination';

const API_URL = 'http://localhost:8080/api/persons';

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

export const personApi = {
  getById: async (id: number): Promise<PersonResponse> => {
    const response = await fetch(`${API_URL}/${id}`, {
      headers: getAuthHeaders(),
    });
    return handleResponse(response);
  },

  create: async (personData: PersonRequest): Promise<PersonResponse> => {
    const response = await fetch(API_URL, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(personData),
    });
    return handleResponse(response);
  },

  update: async (id: number, personData: PersonRequest): Promise<PersonResponse> => {
    const response = await fetch(`${API_URL}/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(personData),
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

  getAll: async (): Promise<PersonResponse[]> => {
    const response = await fetch(API_URL, {
      headers: getAuthHeaders(),
    });
    return handleResponse(response);
  },

  quickCreate: async (personData: QuickCreatePersonRequest): Promise<PersonResponse> => {
    const response = await fetch(`${API_URL}/quick-create`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(personData),
    });
    return handleResponse(response);
  },

  search: async (params: {
    query?: string;
    role?: PersonRole;
    page?: number;
    size?: number;
  }): Promise<PageResponse<PersonResponse>> => {
    const { query, role, page = 0, size = 10 } = params;

    const searchParams = new URLSearchParams();
    if (query) searchParams.append('query', query);
    if (role) searchParams.append('role', role);
    searchParams.append('page', page.toString());
    searchParams.append('size', size.toString());

    const response = await fetch(`${API_URL}/search?${searchParams}`, {
      headers: getAuthHeaders(),
    });

    return handleResponse(response);
  }
};