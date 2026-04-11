import { useApi } from '@/hooks/common/useApi';
import { usePagination } from '@/hooks/common/usePagination';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { auditApi } from '@/api/auditApi';
import type { AuditLogResponse } from '@/types/audit';
import type { PageResponse } from '@/types/pagination';
import { useCallback, useState, useEffect, useRef } from 'react';

export const useAuditLogs = () => {
    const [entityType, setEntityType] = useState<string>();
    const [action, setAction] = useState<string>();
    const [changedBy, setChangedBy] = useState<string>();

    const { params, setPage, setSize, setSort } = usePagination({}, 20);
    const { execute, loading: apiLoading, data, reset } = useApi<PageResponse<AuditLogResponse>>();

    const loading = useDelayedLoading(apiLoading, { delay: 200, minDisplayTime: 300 });
    const initialFetchDone = useRef(false);

    const fetchAuditLogs = useCallback(async () => {
        return execute(() => auditApi.getAll({
            ...params,
            entityType,
            action,
            changedBy
        }));
    }, [execute, params, entityType, action, changedBy]);

    useEffect(() => {
        if (initialFetchDone.current) {
            fetchAuditLogs();
        } else {
            initialFetchDone.current = true;
            fetchAuditLogs();
        }
    }, [params.page, params.size, params.sort, entityType, action, changedBy]);

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
        loading,
        filters: { entityType, action, changedBy },
        setPage,
        setSize,
        setSort,
        applyFilters,
        clearFilters,
        refresh,
        reset,
    };
};