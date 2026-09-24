import { useCallback, useRef } from "react";
import { useApi } from "@/hooks/common/useApi";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";
import { auditApi } from "@/api/auditApi";
import { DEFAULT_PAGE_SIZE_ADMIN } from "@/utils/paginationUtils";
import type { AuditLogResponse } from "@/types/audit";
import type { PageResponse } from "@/types/pagination";

interface AuditLogFilters {
  entityType?: string;
  action?: string;
  changedBy?: string;
}

interface AuditLogsQuery {
  filters: AuditLogFilters;
  page: number;
}

export const useAuditLogs = ({ filters, page }: AuditLogsQuery) => {
  const {
    execute,
    loading: apiLoading,
    data,
  } = useApi<PageResponse<AuditLogResponse>>();

  const executeRef = useRef(execute);
  executeRef.current = execute;

  const loading = useDelayedLoading(apiLoading, {
    delay: 200,
    minDisplayTime: 300,
  });

  const refresh = useCallback(() => {
    return executeRef.current(() =>
      auditApi.admin.getAll({
        page,
        size: DEFAULT_PAGE_SIZE_ADMIN,
        ...filters,
      }),
    );
  }, [page, filters]);

  return {
    auditLogs: data?.content || [],
    pagination: data,
    loading,
    refresh,
  };
};
