import type {
  MovieCreateRequest,
  MovieUpdateRequest,
  MovieCardResponse,
  MovieDetailResponse,
  MovieSessionSearchResponse,
  MovieStatus,
} from '@/types/movie';
import type { PageResponse } from '@/types/pagination';
import { handleApiError } from '@/utils/apiErrorHandler';

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

const getPublicHeaders = (): HeadersInit => {
  return {
    'Content-Type': 'application/json',
  };
};

const fetchApi = async <T>(
  url: string,
  options: RequestInit = {},
  isFormData: boolean = false,
  isPublic: boolean = false
): Promise<T> => {
  const headers = isPublic ? getPublicHeaders() : getAuthHeaders(isFormData);

  const response = await fetch(url, {
    headers,
    ...options,
  });

  if (!response.ok) throw await handleApiError(response);
  if (response.status === 204) return undefined as T;

  return response.json();
};

export const movieApi = {
  public: {
    getById: (id: number): Promise<MovieDetailResponse> =>
      fetchApi<MovieDetailResponse>(`${PUBLIC_URL}/${id}`, {}, false, true),

    getBySlug: (slug: string): Promise<MovieDetailResponse> =>
      fetchApi<MovieDetailResponse>(`${PUBLIC_URL}/slug/${slug}`, {}, false, true),

    getMoviesPaginated: (page?: number, size: number = 12): Promise<PageResponse<MovieCardResponse>> => {
      const params = new URLSearchParams();
      if (page !== undefined) params.append('page', page.toString());
      params.append('size', size.toString());
      params.append('sort', 'title');

      return fetchApi<PageResponse<MovieCardResponse>>(`${PUBLIC_URL}?${params}`, {}, false, true);
    },

    getCurrentlyShowing: (): Promise<MovieCardResponse[]> =>
      fetchApi<MovieCardResponse[]>(`${PUBLIC_URL}/status/current`, {}, false, true),

    getCurrentlyShowingPaginated: (page?: number, size: number = 12): Promise<PageResponse<MovieCardResponse>> => {
      const params = new URLSearchParams();
      if (page !== undefined) params.append('page', page.toString());
      params.append('size', size.toString());
      params.append('sort', 'title');

      return fetchApi<PageResponse<MovieCardResponse>>(`${PUBLIC_URL}/status/current/paginated?${params}`, {}, false, true);
    },

    getUpcoming: (): Promise<MovieCardResponse[]> =>
      fetchApi<MovieCardResponse[]>(`${PUBLIC_URL}/status/upcoming`, {}, false, true),

    getUpcomingPaginated: (page?: number, size: number = 12): Promise<PageResponse<MovieCardResponse>> => {
      const params = new URLSearchParams();
      if (page !== undefined) params.append('page', page.toString());
      params.append('size', size.toString());
      params.append('sort', 'title');

      return fetchApi<PageResponse<MovieCardResponse>>(`${PUBLIC_URL}/status/upcoming/paginated?${params}`, {}, false, true);
    },

    getFilteredMovies: (search?: string, status?: MovieStatus, page?: number, size: number = 20): Promise<PageResponse<MovieCardResponse>> => {
      const params = new URLSearchParams();
      if (search) params.append('search', search);
      if (status) params.append('status', status);
      if (page !== undefined) params.append('page', page.toString());
      params.append('size', size.toString());
      params.append('sort', 'title');

      return fetchApi<PageResponse<MovieCardResponse>>(`${PUBLIC_URL}/filtered?${params}`, {}, false, true);
    },

    getPosterUrl: (id: number): string =>
      `${PUBLIC_URL}/${id}/poster`,
  },

  admin: {
    create: (request: MovieCreateRequest): Promise<MovieDetailResponse> => {
      const formData = new FormData();
      formData.append('movieData', new Blob([JSON.stringify(request)], {
        type: 'application/json',
      }));

      if (request.posterFile) {
        formData.append('posterFile', request.posterFile);
      }

      return fetchApi<MovieDetailResponse>(ADMIN_URL, {
        method: 'POST',
        body: formData,
      }, true);
    },

    update: (id: number, request: MovieUpdateRequest): Promise<MovieDetailResponse> => {
      const formData = new FormData();
      formData.append('movieData', new Blob([JSON.stringify(request)], {
        type: 'application/json',
      }));

      if (request.posterFile) {
        formData.append('posterFile', request.posterFile);
      }

      return fetchApi<MovieDetailResponse>(`${ADMIN_URL}/${id}`, {
        method: 'PUT',
        body: formData,
      }, true);
    },

    delete: (id: number): Promise<void> =>
      fetchApi<void>(`${ADMIN_URL}/${id}`, {
        method: 'DELETE'
      }),

    getArchivedMovies: (page?: number, size: number = 12): Promise<PageResponse<MovieCardResponse>> => {
      const params = new URLSearchParams();
      if (page !== undefined) params.append('page', page.toString());
      params.append('size', size.toString());
      params.append('sort', 'title');

      return fetchApi<PageResponse<MovieCardResponse>>(`${ADMIN_URL}/status/archived?${params}`);
    },

    getByStatus: (status: MovieStatus, page?: number, size: number = 12): Promise<PageResponse<MovieCardResponse>> => {
      const params = new URLSearchParams();
      if (page !== undefined) params.append('page', page.toString());
      params.append('size', size.toString());
      params.append('sort', 'title');

      return fetchApi<PageResponse<MovieCardResponse>>(`${ADMIN_URL}/status/${status}?${params}`);
    },

    search: (search?: string, status?: MovieStatus, page?: number, size: number = 12): Promise<PageResponse<MovieCardResponse>> => {
      const params = new URLSearchParams();
      if (search) params.append('search', search);
      if (status) params.append('status', status);
      if (page !== undefined) params.append('page', page.toString());
      params.append('size', size.toString());
      params.append('sort', 'title');

      return fetchApi<PageResponse<MovieCardResponse>>(`${ADMIN_URL}/search?${params}`);
    },

    searchForSession: (sessionDate: string, search?: string): Promise<MovieSessionSearchResponse[]> => {
      const params = new URLSearchParams({ sessionDate });
      if (search) params.append('search', search);

      return fetchApi<MovieSessionSearchResponse[]>(`${ADMIN_URL}/search/for-session?${params}`);
    },

    quickAddPerson: (request: { name: string; role: string }): Promise<any> =>
      fetchApi<any>(`${ADMIN_URL}/quick-add-person`, {
        method: 'POST',
        body: JSON.stringify(request),
      }),

    searchPersons: (query?: string, role?: string, page?: number, size: number = 10): Promise<PageResponse<any>> => {
      const params = new URLSearchParams();
      if (query) params.append('query', query);
      if (role) params.append('role', role);
      if (page !== undefined) params.append('page', page.toString());
      params.append('size', size.toString());

      return fetchApi<PageResponse<any>>(`${ADMIN_URL}/persons/search?${params}`);
    },

    searchActiveMovies: (search?: string): Promise<MovieSessionSearchResponse[]> => {
      const params = new URLSearchParams();
      if (search) params.append('search', search);

      return fetchApi<MovieSessionSearchResponse[]>(
        `${PUBLIC_URL}/search/active?${params}`,
        {},
        false,
        true
      );
    },
  },
};