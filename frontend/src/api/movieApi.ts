import type {
  MovieCreateRequest,
  MovieUpdateRequest,
  MovieCardResponse,
  MovieDetailResponse
} from '@/types/movie';
import type { PageResponse } from '@/types/pagination';

const API_URL = '/api/movies';

const getAuthHeaders = (isFormData: boolean = false): HeadersInit => {
  const token = localStorage.getItem('authToken');
  const headers: HeadersInit = {};

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  if (!isFormData) {
    headers['Content-Type'] = 'application/json';
  }

  return headers;
};

export const movieApi = {
  getById: async (id: number): Promise<MovieDetailResponse> => {
    const response = await fetch(`${API_URL}/${id}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch movie');
    return response.json();
  },

  getBySlug: async (slug: string): Promise<MovieDetailResponse> => {
    const response = await fetch(`${API_URL}/slug/${slug}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch movie');
    return response.json();
  },

  getAll: async (): Promise<MovieDetailResponse[]> => {
    const response = await fetch(API_URL, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch movies');
    return response.json();
  },

  getPaginated: async (page: number = 0, size: number = 10): Promise<PageResponse<MovieDetailResponse>> => {
    const response = await fetch(`${API_URL}/paginated?page=${page}&size=${size}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch movies');
    return response.json();
  },

  getCurrentlyShowing: async (): Promise<MovieCardResponse[]> => {
    const response = await fetch(`${API_URL}/status/current`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch currently showing movies');
    return response.json();
  },

  getUpcoming: async (): Promise<MovieCardResponse[]> => {
    const response = await fetch(`${API_URL}/status/upcoming`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch upcoming movies');
    return response.json();
  },

  getArchived: async (): Promise<MovieCardResponse[]> => {
    const response = await fetch(`${API_URL}/status/archived`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch archived movies');
    return response.json();
  },

  getMoviesForSessions: async (): Promise<MovieCardResponse[]> => {
    const response = await fetch(`${API_URL}/for-sessions`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch movies for sessions');
    return response.json();
  },

  create: async (movieData: MovieCreateRequest, posterFile: File): Promise<MovieDetailResponse> => {
    const formData = new FormData();

    const { posterFile: _, ...requestData } = movieData;

    formData.append('movieData', new Blob([JSON.stringify(requestData)], {
      type: 'application/json'
    }));

    formData.append('posterFile', posterFile);

    const response = await fetch(API_URL, {
      method: 'POST',
      headers: getAuthHeaders(true),
      body: formData,
    });

    if (!response.ok) throw new Error('Failed to create movie');
    return response.json();
  },

  update: async (id: number, movieData: MovieUpdateRequest, posterFile?: File): Promise<MovieDetailResponse> => {
    const formData = new FormData();

    const { posterFile: _, removePoster, ...updateData } = movieData;

    formData.append('movieData', new Blob([JSON.stringify({
      ...updateData,
      removePoster: removePoster || false
    })], {
      type: 'application/json'
    }));

    if (posterFile) {
      formData.append('posterFile', posterFile);
    }

    const response = await fetch(`${API_URL}/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(true),
      body: formData,
    });

    if (!response.ok) throw new Error('Failed to update movie');
    return response.json();
  },

  delete: async (id: number): Promise<void> => {
    const response = await fetch(`${API_URL}/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to delete movie');
  },

  getPoster: async (id: number): Promise<Blob> => {
    const response = await fetch(`${API_URL}/${id}/poster`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch poster');
    return response.blob();
  },

  getPosterUrl: (id: number): string => {
    return `${API_URL}/${id}/poster`;
  },

  getPosterUrlWithTimestamp: (id: number): string => {
    return `${API_URL}/${id}/poster?t=${Date.now()}`;
  }
};