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

const BASE_URL = '/api/movies';
const ADMIN_BASE_URL = '/api/admin/movies';

export const movieApi = {
  public: {
    getBySlug: (slug: string) =>
      api.get<MovieDetailResponse>(`${BASE_URL}/slug/${slug}`),

    getNowShowingForHome: () =>
      api.get<MovieCardResponse[]>(`${BASE_URL}/now-showing/home`),

    getComingSoonForHome: () =>
      api.get<MovieCardResponse[]>(`${BASE_URL}/coming-soon/home`),

    getLeavingSoonForHome: () =>
      api.get<MovieCardResponse[]>(`${BASE_URL}/leaving-soon/home`),

    getCurrentlyShowing: (params?: SearchParams) =>
      api.get<PageResponse<MovieCardResponse>>(`${BASE_URL}/currently-showing`, { params }),

    getUpcoming: (params?: SearchParams) =>
      api.get<PageResponse<MovieCardResponse>>(`${BASE_URL}/upcoming`, { params }),
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

      return api.post<MovieDetailResponse>(ADMIN_BASE_URL, formData, {
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

      return api.put<MovieDetailResponse>(`${ADMIN_BASE_URL}/${id}`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
    },

    delete: (id: number) =>
      api.delete<void>(`${ADMIN_BASE_URL}/${id}`),

    getMovies: (params?: SearchParams & {
      title?: string;
      status?: MovieStatus;
    }) => api.get<PageResponse<MovieCardResponse>>(ADMIN_BASE_URL, { params }),

    getMovieById: (id: number) =>
      api.get<MovieDetailResponse>(`${ADMIN_BASE_URL}/${id}`),

    searchMoviesForSession: (search?: string) =>
      api.get<MovieSessionSearchResponse[]>(`${ADMIN_BASE_URL}/search/session`, {
        params: search ? { search } : undefined
      }),
  },
};