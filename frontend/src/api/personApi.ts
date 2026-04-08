import { api } from '@/services/api';
import type { PersonResponse, PersonRequest, QuickCreatePersonRequest, PersonListResponse, PersonRole } from '@/types/person';
import type { PageResponse, SearchParams } from '@/types/pagination';

const ADMIN_BASE_URL = '/api/admin/persons';

export const personApi = {
  admin: {
    create: (request: PersonRequest) =>
      api.post<PersonResponse>(ADMIN_BASE_URL, request),

    quickCreate: (request: QuickCreatePersonRequest) =>
      api.post<PersonResponse>(`${ADMIN_BASE_URL}/quick`, request),

    getById: (id: number) =>
      api.get<PersonResponse>(`${ADMIN_BASE_URL}/${id}`),

    update: (id: number, request: PersonRequest) =>
      api.put<PersonResponse>(`${ADMIN_BASE_URL}/${id}`, request),

    delete: (id: number) =>
      api.delete<void>(`${ADMIN_BASE_URL}/${id}`),

    getAll: (params?: SearchParams & { name?: string; role?: PersonRole }) =>
      api.get<PageResponse<PersonListResponse>>(ADMIN_BASE_URL, { params }),
  }
};