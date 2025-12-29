import type {
  MovieCreateRequest,
  MovieUpdateRequest,
  MovieCardResponse,
  MovieDetailResponse,
  MovieSessionSearchResponse,
  MovieFilter
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

  getMoviesPaginated: async (page: number = 0, size: number = 12): Promise<PageResponse<MovieDetailResponse>> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });

    const response = await fetch(`${PUBLIC_API_URL}?${params}`, {
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

  getCurrentlyShowingMoviesPaginated: async (page: number = 0, size: number = 12): Promise<PageResponse<MovieCardResponse>> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });

    const response = await fetch(`${PUBLIC_API_URL}/status/current/paginated?${params}`, {
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

  getUpcomingMoviesPaginated: async (page: number = 0, size: number = 12): Promise<PageResponse<MovieCardResponse>> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });

    const response = await fetch(`${PUBLIC_API_URL}/status/upcoming/paginated?${params}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  getArchivedMovies: async (): Promise<MovieCardResponse[]> => {
    const response = await fetch(`${ADMIN_API_URL}/status/archived`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  getArchivedMoviesPaginated: async (page: number = 0, size: number = 12): Promise<PageResponse<MovieCardResponse>> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });

    const response = await fetch(`${ADMIN_API_URL}/status/archived/paginated?${params}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  getNewReleases: async (limit: number = 5): Promise<MovieCardResponse[]> => {
    const params = new URLSearchParams({
      limit: limit.toString(),
    });

    const response = await fetch(`${PUBLIC_API_URL}/new-releases?${params}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  getEndingSoon: async (limit: number = 5): Promise<MovieCardResponse[]> => {
    const params = new URLSearchParams({
      limit: limit.toString(),
    });

    const response = await fetch(`${PUBLIC_API_URL}/ending-soon?${params}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  getFilteredMovies: async (filter: Partial<MovieFilter>): Promise<PageResponse<MovieCardResponse>> => {
    const params = new URLSearchParams();

    Object.entries(filter).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params.append(key, String(value));
      }
    });

    const response = await fetch(`${PUBLIC_API_URL}/filtered?${params}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw await handleApiError(response);
    return response.json();
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
  },

  searchMoviesForSessionCreation: async (sessionDate: string, search?: string): Promise<MovieSessionSearchResponse[]> => {
    const params = new URLSearchParams({
      sessionDate: sessionDate,
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

  createMovie: async (movieData: MovieCreateRequest): Promise<MovieDetailResponse> => {
    const formData = new FormData();
    const { posterFile, ...requestData } = movieData;

    formData.append('movieData', new Blob([JSON.stringify(requestData)], {
      type: 'application/json',
    }));

    if (posterFile) {
      formData.append('posterFile', posterFile);
    }

    const response = await fetch(ADMIN_API_URL, {
      method: 'POST',
      headers: getAuthHeaders(true),
      body: formData,
    });

    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  updateMovie: async (id: number, movieData: MovieUpdateRequest): Promise<MovieDetailResponse> => {
    const formData = new FormData();
    const { posterFile, ...requestData } = movieData;

    formData.append('movieData', new Blob([JSON.stringify(requestData)], {
      type: 'application/json',
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

  searchPersonsForMovie: async (params: {
    query?: string;
    role?: string;
    page?: number;
    size?: number;
  }): Promise<PageResponse<any>> => {
    const searchParams = new URLSearchParams();

    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        searchParams.append(key, String(value));
      }
    });

    const response = await fetch(`${ADMIN_API_URL}/persons/search?${searchParams}`, {
      headers: getAuthHeaders(),
    });

    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },

  quickAddPerson: async (request: { name: string; role: string }): Promise<any> => {
    const response = await fetch(`${ADMIN_API_URL}/quick-add-person`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(request),
    });

    if (!response.ok) throw await handleApiError(response);
    return response.json();
  },
};