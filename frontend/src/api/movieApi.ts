import { api } from '@/services/api';
import type {
  MovieCreateRequest,
  MovieUpdateRequest,
  MovieCardResponse,
  MovieDetailResponse,
  MovieSessionSearchResponse
} from '@/types/movie';
import type { PageResponse, SearchParams } from '@/types/pagination';
import type { MovieStatus } from '@/types/movie';

const PUBLIC_URL = '/api/movies';
const ADMIN_URL = '/api/admin/movies';

export const movieApi = {
  public: {
    getById: (id: number) =>
      api.get<MovieDetailResponse>(`${PUBLIC_URL}/${id}`),

    getBySlug: (slug: string) =>
      api.get<MovieDetailResponse>(`${PUBLIC_URL}/slug/${slug}`),

    getCurrentlyShowing: (params?: SearchParams) =>
      api.get<PageResponse<MovieCardResponse>>(`${PUBLIC_URL}/currently-showing`, { params }),

    getUpcoming: (params?: SearchParams) =>
      api.get<PageResponse<MovieCardResponse>>(`${PUBLIC_URL}/upcoming`, { params }),
  },

  admin: {
    create: (request: MovieCreateRequest) => {
      const formData = new FormData();
      const { posterFile, ...requestData } = request;

      formData.append('movieData', new Blob([JSON.stringify(requestData)], {
        type: 'application/json',
      }));

      if (posterFile) {
        formData.append('posterFile', posterFile);
      }

      return api.post<MovieDetailResponse>(ADMIN_URL, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
    },

    update: (id: number, request: MovieUpdateRequest) => {
      const formData = new FormData();
      const { posterFile, ...requestData } = request;

      formData.append('movieData', new Blob([JSON.stringify(requestData)], {
        type: 'application/json',
      }));

      if (posterFile) {
        formData.append('posterFile', posterFile);
      }

      return api.put<MovieDetailResponse>(`${ADMIN_URL}/${id}`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
    },

    delete: (id: number) =>
      api.delete<void>(`${ADMIN_URL}/${id}`),

    getMovies: (params?: {
      title?: string;
      status?: MovieStatus;
      page?: number;
      size?: number;
      sort?: string;
    }) => api.get<PageResponse<MovieCardResponse>>(ADMIN_URL, { params }),

    getMovieById: (id: number) =>
      api.get<MovieDetailResponse>(`${ADMIN_URL}/${id}`),

    getMovieBySlug: (slug: string) =>
      api.get<MovieDetailResponse>(`${ADMIN_URL}/by-slug/${slug}`),

    searchMoviesForSession: (search?: string) =>
      api.get<MovieSessionSearchResponse[]>(`${ADMIN_URL}/search/session`, {
        params: search ? { search } : undefined
      }),
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