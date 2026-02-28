import { api } from '@/services/api';
import type { GenreResponse, GenreRequest } from '@/types/genre';
import type { PageResponse, SearchParams } from '@/types/pagination';

const PUBLIC_URL = '/api/genres';
const ADMIN_URL = '/api/admin/genres';

export const genreApi = {
  public: {
    search: (query: string, limit: number = 10) =>
      api.get<GenreResponse[]>(`${PUBLIC_URL}/search`, {
        params: { query, limit }
      }),
  },

  admin: {
    create: (request: GenreRequest) =>
      api.post<GenreResponse>(ADMIN_URL, request),

    getById: (id: number) =>
      api.get<GenreResponse>(`${ADMIN_URL}/${id}`),

    update: (id: number, request: GenreRequest) =>
      api.put<GenreResponse>(`${ADMIN_URL}/${id}`, request),

    delete: (id: number) =>
      api.delete<void>(`${ADMIN_URL}/${id}`),

    getAll: (params?: SearchParams) =>
      api.get<PageResponse<GenreResponse>>(ADMIN_URL, { params }),
  }
};