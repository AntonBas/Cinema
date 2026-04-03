import { api } from '@/services/api';
import type { PageResponse, SearchParams } from '@/types/pagination';
import type { AuditLogResponse } from '@/types/audit';

const BASE_URL = '/admin/audit-logs';

export const auditApi = {
    getAll: (params?: SearchParams & {
        entityType?: string;
        action?: string;
        changedBy?: string;
    }) =>
        api.get<PageResponse<AuditLogResponse>>(BASE_URL, { params }),

    getEntityHistory: (entityType: string, entityId: number) =>
        api.get<AuditLogResponse[]>(`${BASE_URL}/entity/${entityType}/${entityId}`)
};