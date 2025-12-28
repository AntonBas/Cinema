import type {
  MovieCreateRequest,
  MovieUpdateRequest,
  MovieCardResponse,
  MovieDetailResponse,
  MovieSessionSearchResponse
} from '@/types/movie';
import type { PageResponse } from '@/types/pagination';
import { handleApiError } from '@/utils/apiErrorHandler';

const PUBLIC_API_URL = '/api/movies';
const ADMIN_API_URL = '/api/admin/movies';

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
  getMovieById: async (id: number): Promise<MovieDetailResponse> => {
    const response = await fetch(`${PUBLIC_API_URL}/${id}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  getMovieBySlug: async (slug: string): Promise<MovieDetailResponse> => {
    const response = await fetch(`${PUBLIC_API_URL}/slug/${slug}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  getAllMovies: async (): Promise<MovieDetailResponse[]> => {
    const response = await fetch(PUBLIC_API_URL, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  getMoviesPaginated: async (page: number = 0, size: number = 10): Promise<PageResponse<MovieDetailResponse>> => {
    const response = await fetch(`${PUBLIC_API_URL}/paginated?page=${page}&size=${size}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  getCurrentlyShowingMovies: async (): Promise<MovieCardResponse[]> => {
    const response = await fetch(`${PUBLIC_API_URL}/status/current`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  getUpcomingMovies: async (): Promise<MovieCardResponse[]> => {
    const response = await fetch(`${PUBLIC_API_URL}/status/upcoming`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  getArchivedMovies: async (): Promise<MovieCardResponse[]> => {
    const response = await fetch(`${PUBLIC_API_URL}/status/archived`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  searchMovies: async (search: string, page: number = 0, size: number = 10): Promise<PageResponse<MovieCardResponse>> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString()
    });

    if (search) {
      params.append('search', search);
    }

    const response = await fetch(`${PUBLIC_API_URL}/search?${params}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  searchMoviesForSessionCreation: async (sessionDate: string, search?: string): Promise<MovieSessionSearchResponse[]> => {
    const params = new URLSearchParams({
      sessionDate: sessionDate
    });

    if (search) {
      params.append('search', search);
    }

    const response = await fetch(`${ADMIN_API_URL}/search/for-session?${params}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  createMovie: async (movieData: MovieCreateRequest, posterFile: File): Promise<MovieDetailResponse> => {
    const formData = new FormData();

    const { posterFile: _, ...requestData } = movieData;

    formData.append('movieData', new Blob([JSON.stringify(requestData)], {
      type: 'application/json'
    }));

    formData.append('posterFile', posterFile);

    const response = await fetch(ADMIN_API_URL, {
      method: 'POST',
      headers: getAuthHeaders(true),
      body: formData,
    });

    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  updateMovie: async (id: number, movieData: MovieUpdateRequest, posterFile?: File): Promise<MovieDetailResponse> => {
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

    const response = await fetch(`${ADMIN_API_URL}/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(true),
      body: formData,
    });

    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  deleteMovie: async (id: number): Promise<void> => {
    const response = await fetch(`${ADMIN_API_URL}/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw await handleApiError(response);
  },

  getMoviePoster: async (id: number): Promise<Blob> => {
    const response = await fetch(`${PUBLIC_API_URL}/${id}/poster`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.blob();
  },

  getMoviePosterUrl: (id: number): string => {
    return `${PUBLIC_API_URL}/${id}/poster`;
  },

  getMoviePosterUrlWithTimestamp: (id: number): string => {
    return `${PUBLIC_API_URL}/${id}/poster?t=${Date.now()}`;
  }
};