import type { PersonDto, PersonRequest, PersonRole, QuickCreatePersonDto } from '../types/person';
import type { PageResponse } from '../types/pagination';

const API_URL = 'http://localhost:8080/api/persons';

const getAuthHeaders = () => {
  const token = localStorage.getItem('authToken');
  return {
    'Content-Type': 'application/json',
    'Authorization': token ? `Bearer ${token}` : '',
  };
};

export const personApi = {
  getById: async (id: number): Promise<PersonDto> => {
    const response = await fetch(`${API_URL}/${id}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch person');
    return response.json();
  },

  create: async (personData: PersonRequest): Promise<PersonDto> => {
    const response = await fetch(API_URL, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(personData),
    });
    if (!response.ok) throw new Error('Failed to create person');
    return response.json();
  },

  update: async (id: number, personData: PersonRequest): Promise<PersonDto> => {
    const response = await fetch(`${API_URL}/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(personData),
    });
    if (!response.ok) throw new Error('Failed to update person');
    return response.json();
  },

  delete: async (id: number): Promise<void> => {
    const response = await fetch(`${API_URL}/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to delete person');
  },

  getAll: async (): Promise<PersonDto[]> => {
    const response = await fetch(API_URL, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch persons');
    return response.json();
  },

  quickCreate: async (personData: QuickCreatePersonDto): Promise<PersonDto> => {
    const response = await fetch(`${API_URL}/quick-create`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(personData),
    });
    if (!response.ok) throw new Error('Failed to quick create person');
    return response.json();
  },

  search: async (params: {
    query?: string;
    role?: PersonRole;
    page?: number;
    size?: number;
  }): Promise<PageResponse<PersonDto>> => {
    const { query, role, page = 0, size = 10 } = params;

    const searchParams = new URLSearchParams();
    if (query) searchParams.append('query', query);
    if (role) searchParams.append('role', role);
    searchParams.append('page', page.toString());
    searchParams.append('size', size.toString());

    const response = await fetch(`${API_URL}/search?${searchParams}`, {
      headers: getAuthHeaders(),
    });

    if (!response.ok) throw new Error('Failed to search persons');
    return response.json();
  }
};