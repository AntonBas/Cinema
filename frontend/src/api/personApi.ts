import { api } from '@/services/api';
import type { PersonResponse, PersonRequest, QuickCreatePersonRequest } from '@/types/person';
import type { PageResponse, SearchParams } from '@/types/pagination';

const PUBLIC_URL = '/api/persons';
const ADMIN_URL = '/api/admin/persons';

export const personApi = {
  public: {
    getById: (id: number) =>
      api.get<PersonResponse>(`${PUBLIC_URL}/${id}`),
  },

  admin: {
    create: (request: PersonRequest) =>
      api.post<PersonResponse>(ADMIN_URL, request),

    quickCreate: (request: QuickCreatePersonRequest) =>
      api.post<PersonResponse>(`${ADMIN_URL}/quick`, request),

    getById: (id: number) =>
      api.get<PersonResponse>(`${ADMIN_URL}/${id}`),

    update: (id: number, request: PersonRequest) =>
      api.put<PersonResponse>(`${ADMIN_URL}/${id}`, request),

    delete: (id: number) =>
      api.delete<void>(`${ADMIN_URL}/${id}`),

    getAll: (params?: SearchParams & { name?: string; role?: string }) =>
      api.get<PageResponse<PersonResponse>>(ADMIN_URL, { params }),
  }
};

export const personKeys = {
  all: ['persons'] as const,
  admin: {
    all: ['persons', 'admin'] as const,
    lists: () => [...personKeys.admin.all, 'list'] as const,
    list: (params?: SearchParams & { name?: string; role?: string }) =>
      [...personKeys.admin.lists(), params] as const,
    details: () => [...personKeys.admin.all, 'detail'] as const,
    detail: (id: number) => [...personKeys.admin.details(), id] as const,
  },
  public: {
    all: ['persons', 'public'] as const,
    detail: (id: number) => [...personKeys.public.all, id] as const,
  }
};