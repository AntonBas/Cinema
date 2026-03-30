import { useApi } from '@/hooks/common/useApi';
import { usePagination } from '@/hooks/common/usePagination';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { auditApi } from '@/api/auditApi';
import type { AuditLogResponse } from '@/types/audit';
import type { PageResponse } from '@/types/pagination';
import { useCallback, useState, useEffect } from 'react';

export const useAuditLogs = () => {
    const [entityType, setEntityType] = useState<string>();
    const [action, setAction] = useState<string>();
    const [changedBy, setChangedBy] = useState<string>();

    const { params, setPage, setSize, setSort } = usePagination({}, 20);

    const { execute, loading: apiLoading, data, reset } = useApi<PageResponse<AuditLogResponse>>();

    const isLoading = useDelayedLoading(apiLoading, { delay: 200, minDisplayTime: 300 });

    const fetchAuditLogs = useCallback(async () => {
        const response = await execute(
            () => auditApi.getAll({
                page: params.page,
                size: params.size,
                sort: params.sort,
                entityType,
                action,
                changedBy
            }),
            {
                showErrorNotification: true,
                cacheKey: `audit-logs-${JSON.stringify({ params, entityType, action, changedBy })}`,
                cacheTime: 0
            }
        );
        return response;
    }, [execute, params.page, params.size, params.sort, entityType, action, changedBy]);

    useEffect(() => {
        fetchAuditLogs();
    }, [fetchAuditLogs]);

    const refresh = useCallback(() => {
        return fetchAuditLogs();
    }, [fetchAuditLogs]);

    const applyFilters = useCallback((filters: {
        entityType?: string;
        action?: string;
        changedBy?: string;
    }) => {
        setEntityType(filters.entityType);
        setAction(filters.action);
        setChangedBy(filters.changedBy);
        setPage(0);
    }, [setPage]);

    const clearFilters = useCallback(() => {
        setEntityType(undefined);
        setAction(undefined);
        setChangedBy(undefined);
        setPage(0);
    }, [setPage]);

    return {
        auditLogs: data?.content || [],
        pagination: data ? {
            currentPage: data.number,
            totalPages: data.totalPages,
            pageSize: data.size,
            totalElements: data.totalElements,
            hasNext: data.hasNext,
            hasPrevious: data.hasPrevious,
            isFirst: data.first,
            isLast: data.last,
            isEmpty: data.empty
        } : null,
        loading: isLoading,
        filters: { entityType, action, changedBy },
        setPage,
        setSize,
        setSort,
        applyFilters,
        clearFilters,
        refresh,
        reset
    };
};