import { api } from "@/services/api";
import type {
  PersonResponse,
  PersonRequest,
  PersonListResponse,
  PersonRole,
} from "@/types/person";
import type { PageResponse, SearchParams } from "@/types/pagination";

const ADMIN_BASE_URL = "/api/admin/persons";

export const personApi = {
  admin: {
    create: (request: PersonRequest) =>
      api.post<PersonResponse>(ADMIN_BASE_URL, request),

    getAll: (params?: SearchParams & { query?: string; role?: PersonRole }) =>
      api.get<PageResponse<PersonListResponse>>(ADMIN_BASE_URL, { params }),

    update: (id: number, request: PersonRequest) =>
      api.put<PersonResponse>(`${ADMIN_BASE_URL}/${id}`, request),

    delete: (id: number) => api.delete<void>(`${ADMIN_BASE_URL}/${id}`),
  },
};
