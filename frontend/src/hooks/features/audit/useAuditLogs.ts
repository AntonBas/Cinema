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
                ...params,
                entityType,
                action,
                changedBy
            }),
            {
                showErrorNotification: true,
            }
        );
        return response;
    }, [execute, params, entityType, action, changedBy]);

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
        pagination: data,
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