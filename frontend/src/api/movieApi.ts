import type {
  MovieCreateRequest,
  MovieUpdateRequest,
  MovieCardResponse,
  MovieDetailResponse,
  MovieSessionSearchResponse,
  MovieFilterRequest
} from '@/types/movie';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { handleApiError } from '@/utils/apiErrorHandler';
import { buildPagedUrl } from '@/utils/paginationUtils';

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
      const url = buildPagedUrl(`${PUBLIC_URL}/currently-showing`, params, 'grid');
      return fetchApi<PageResponse<MovieCardResponse>>(url, {}, true);
    },

    getUpcoming: (params?: SearchParams): Promise<PageResponse<MovieCardResponse>> => {
      const url = buildPagedUrl(`${PUBLIC_URL}/upcoming`, params, 'grid');
      return fetchApi<PageResponse<MovieCardResponse>>(url, {}, true);
    },

    getPosterUrl: (id: number): string =>
      `${PUBLIC_URL}/${id}/poster`,
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

    getFilteredMovies: (filter?: MovieFilterRequest, params?: SearchParams): Promise<PageResponse<MovieCardResponse>> => {
      const filterParams = { ...params, ...filter };
      const url = buildPagedUrl(ADMIN_URL, filterParams, 'admin');
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