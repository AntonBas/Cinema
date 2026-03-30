import { api } from '@/services/api';
import type { PageResponse } from '@/types/pagination';
import type { AuditLogResponse } from '@/types/audit';

const BASE_URL = '/admin/audit-logs';

export const auditApi = {
    getAll: (params?: {
        entityType?: string;
        action?: string;
        changedBy?: string;
        page?: number;
        size?: number;
        sort?: string;
    }) => api.get<PageResponse<AuditLogResponse>>(BASE_URL, { params }),

    getEntityHistory: (entityType: string, entityId: number) =>
        api.get<AuditLogResponse[]>(`${BASE_URL}/entity/${entityType}/${entityId}`)
};