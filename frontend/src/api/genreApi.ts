import { api } from "@/services/api";
import type {
  GenreRequest,
  GenreResponse,
  GenreListResponse,
} from "@/types/genre";
import type { PageResponse, SearchParams } from "@/types/pagination";

const BASE_URL = "/api/admin/genres";

export const genreApi = {
  getAll: (params?: SearchParams & { query?: string }) =>
    api.get<PageResponse<GenreListResponse>>(BASE_URL, { params }),

  create: (request: GenreRequest) => api.post<GenreResponse>(BASE_URL, request),

  update: (id: number, request: GenreRequest) =>
    api.put<GenreResponse>(`${BASE_URL}/${id}`, request),

  delete: (id: number) => api.delete<void>(`${BASE_URL}/${id}`),
};
