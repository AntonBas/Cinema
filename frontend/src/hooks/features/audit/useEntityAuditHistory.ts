import { useCallback, useRef } from "react";
import { useApi } from "@/hooks/common/useApi";
import { auditApi } from "@/api/auditApi";
import type { AuditLogResponse } from "@/types/audit";

export const useEntityAuditHistory = () => {
  const { data, loading, error, execute, reset } =
    useApi<AuditLogResponse[]>();
  const executeRef = useRef(execute);
  executeRef.current = execute;

  const getEntityHistory = useCallback(
    async (entityType: string, entityId: number) => {
      return executeRef.current(
        () => auditApi.getEntityHistory(entityType, entityId),
        { showErrorNotification: false },
      );
    },
    [],
  );

  return {
    history: data || [],
    loading,
    error,
    getEntityHistory,
    reset,
  };
};
