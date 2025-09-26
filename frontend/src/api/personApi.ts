import type { PersonDto, PersonFormData } from '../types/Person';

const API_URL = 'http://localhost:8080/api/persons';

const getAuthHeaders = () => {
  const token = localStorage.getItem('authToken');
  return {
    'Content-Type': 'application/json',
    'Authorization': token ? `Bearer ${token}` : '',
  };
};

export const personApi = {
  getAll: async (): Promise<PersonDto[]> => {
    const response = await fetch(API_URL, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch persons');
    return response.json();
  },

  getById: async (id: number): Promise<PersonDto> => {
    const response = await fetch(`${API_URL}/${id}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch person');
    return response.json();
  },

  create: async (personData: PersonFormData): Promise<PersonDto> => {
    const response = await fetch(API_URL, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(personData),
    });
    if (!response.ok) throw new Error('Failed to create person');
    return response.json();
  },

  update: async (id: number, personData: PersonFormData): Promise<PersonDto> => {
    const response = await fetch(`${API_URL}/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify({ ...personData, id }),
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
  }
};