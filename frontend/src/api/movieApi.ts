import { api } from '@/services/api';
import type {
  MovieCreateRequest,
  MovieUpdateRequest,
  MovieCardResponse,
  MovieDetailResponse,
  MovieAdminResponse,
  MovieSessionSearchResponse,
  MovieStatus
} from '@/types/movie';
import type { PageResponse, SearchParams } from '@/types/pagination';

const BASE_URL = '/api/movies';
const ADMIN_BASE_URL = '/api/admin/movies';

export const movieApi = {
  public: {
    getBySlug: (slug: string) =>
      api.get<MovieDetailResponse>(`${BASE_URL}/slug/${slug}`),

    getCurrentMoviesForHome: () =>
      api.get<MovieCardResponse[]>(`${BASE_URL}/current/home`),

    getUpcomingMoviesForHome: () =>
      api.get<MovieCardResponse[]>(`${BASE_URL}/upcoming/home`),

    getLeavingSoonForHome: () =>
      api.get<MovieCardResponse[]>(`${BASE_URL}/leaving-soon/home`),

    getCurrentlyShowing: (params?: SearchParams) =>
      api.get<PageResponse<MovieCardResponse>>(`${BASE_URL}/currently-showing`, { params }),

    getUpcoming: (params?: SearchParams) =>
      api.get<PageResponse<MovieCardResponse>>(`${BASE_URL}/upcoming`, { params }),

    getPoster: (id: number) =>
      api.get<ArrayBuffer>(`${BASE_URL}/${id}/poster`, { responseType: 'arraybuffer' }),

    search: (query?: string, date?: string) =>
      api.get<MovieSessionSearchResponse[]>(`${BASE_URL}/search`, {
        params: { query, date }
      }),
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

      return api.post<MovieAdminResponse>(ADMIN_BASE_URL, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
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

      return api.put<MovieAdminResponse>(`${ADMIN_BASE_URL}/${id}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
    },

    delete: (id: number) =>
      api.delete<void>(`${ADMIN_BASE_URL}/${id}`),

    getMovies: (params?: SearchParams & {
      query?: string;
      status?: MovieStatus;
    }) => api.get<PageResponse<MovieCardResponse>>(ADMIN_BASE_URL, { params }),

    getById: (id: number) =>
      api.get<MovieAdminResponse>(`${ADMIN_BASE_URL}/${id}`),

    search: (query?: string) =>
      api.get<MovieSessionSearchResponse[]>(`${ADMIN_BASE_URL}/search`, {
        params: query ? { query } : undefined
      }),
  },
};