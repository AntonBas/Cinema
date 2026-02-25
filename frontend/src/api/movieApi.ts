import type {
  MovieCreateRequest,
  MovieUpdateRequest,
  MovieCardResponse,
  MovieDetailResponse,
  MovieSessionSearchResponse
} from '@/types/movie';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { handleApiError } from '@/utils/apiErrorHandler';
import { buildFilteredUrl } from '@/utils/paginationUtils';
import type { MovieStatus } from '@/types/movie';

const PUBLIC_URL = '/api/movies';
const ADMIN_URL = '/api/admin/movies';

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

const fetchApi = async <T>(
  url: string,
  options: RequestInit = {},
  isPublic: boolean = false,
  isFormData: boolean = false
): Promise<T> => {
  const headers = isPublic ? { 'Content-Type': 'application/json' } : getAuthHeaders(isFormData);

  const response = await fetch(url, {
    headers,
    ...options,
    credentials: 'include',
  });

  if (!response.ok) throw await handleApiError(response);
  if (response.status === 204) return undefined as T;

  return response.json();
};

export const movieApi = {
  public: {
    getById: (id: number): Promise<MovieDetailResponse> =>
      fetchApi<MovieDetailResponse>(`${PUBLIC_URL}/${id}`, {}, true),

    getBySlug: (slug: string): Promise<MovieDetailResponse> =>
      fetchApi<MovieDetailResponse>(`${PUBLIC_URL}/slug/${slug}`, {}, true),

    getCurrentlyShowing: (params?: SearchParams): Promise<PageResponse<MovieCardResponse>> => {
      const url = buildFilteredUrl(`${PUBLIC_URL}/currently-showing`, params);
      return fetchApi<PageResponse<MovieCardResponse>>(url, {}, true);
    },

    getUpcoming: (params?: SearchParams): Promise<PageResponse<MovieCardResponse>> => {
      const url = buildFilteredUrl(`${PUBLIC_URL}/upcoming`, params);
      return fetchApi<PageResponse<MovieCardResponse>>(url, {}, true);
    },
  },

  admin: {
    create: (request: MovieCreateRequest): Promise<MovieDetailResponse> => {
      const formData = new FormData();
      const { posterFile, ...requestData } = request;

      formData.append('movieData', new Blob([JSON.stringify(requestData)], {
        type: 'application/json',
      }));

      if (posterFile) {
        formData.append('posterFile', posterFile);
      }

      return fetchApi<MovieDetailResponse>(ADMIN_URL, {
        method: 'POST',
        body: formData,
      }, false, true);
    },

    update: (id: number, request: MovieUpdateRequest): Promise<MovieDetailResponse> => {
      const formData = new FormData();
      const { posterFile, ...requestData } = request;

      formData.append('movieData', new Blob([JSON.stringify(requestData)], {
        type: 'application/json',
      }));

      if (posterFile) {
        formData.append('posterFile', posterFile);
      }

      return fetchApi<MovieDetailResponse>(`${ADMIN_URL}/${id}`, {
        method: 'PUT',
        body: formData,
      }, false, true);
    },

    delete: (id: number): Promise<void> =>
      fetchApi<void>(`${ADMIN_URL}/${id}`, {
        method: 'DELETE'
      }, false),

    getMovies: (params?: {
      title?: string;
      status?: MovieStatus;
      page?: number;
      size?: number;
      sort?: string;
    }): Promise<PageResponse<MovieCardResponse>> => {
      const url = buildFilteredUrl(ADMIN_URL, params);
      return fetchApi<PageResponse<MovieCardResponse>>(url, {}, false);
    },

    getMovieById: (id: number): Promise<MovieDetailResponse> =>
      fetchApi<MovieDetailResponse>(`${ADMIN_URL}/${id}`, {}, false),

    getMovieBySlug: (slug: string): Promise<MovieDetailResponse> =>
      fetchApi<MovieDetailResponse>(`${ADMIN_URL}/by-slug/${slug}`, {}, false),

    searchMoviesForSession: (search?: string): Promise<MovieSessionSearchResponse[]> => {
      const url = `${ADMIN_URL}/search/session${search ? `?search=${encodeURIComponent(search)}` : ''}`;
      return fetchApi<MovieSessionSearchResponse[]>(url, {}, false);
    },
  },
};

export const movieKeys = {
  all: ['movies'] as const,
  public: {
    all: ['movies', 'public'] as const,
    currentlyShowing: (params?: SearchParams) =>
      [...movieKeys.public.all, 'currentlyShowing', params] as const,
    upcoming: (params?: SearchParams) =>
      [...movieKeys.public.all, 'upcoming', params] as const,
    detail: (id: number) => [...movieKeys.public.all, 'detail', id] as const,
    slug: (slug: string) => [...movieKeys.public.all, 'slug', slug] as const,
  },
  admin: {
    all: ['movies', 'admin'] as const,
    lists: () => [...movieKeys.admin.all, 'list'] as const,
    list: (params?: { title?: string; status?: MovieStatus; page?: number; size?: number; sort?: string }) =>
      [...movieKeys.admin.lists(), params] as const,
    details: () => [...movieKeys.admin.all, 'detail'] as const,
    detail: (id: number) => [...movieKeys.admin.details(), id] as const,
    slug: (slug: string) => [...movieKeys.admin.all, 'slug', slug] as const,
    sessionSearch: (search?: string) =>
      [...movieKeys.admin.all, 'sessionSearch', search] as const,
  },
};