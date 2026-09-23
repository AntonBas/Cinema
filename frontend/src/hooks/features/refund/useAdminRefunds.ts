import { useCallback, useRef, useState } from "react";
import { useApi } from "@/hooks/common/useApi";
import { usePagination } from "@/hooks/common/usePagination";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";
import { refundApi } from "@/api/refundApi";
import { DEFAULT_PAGE_SIZE_ADMIN } from "@/utils/paginationUtils";
import type {
  AdminRefundFilters,
  AdminRefundListResponse,
} from "@/types/refund";
import type { PageResponse } from "@/types/pagination";

export const useAdminRefunds = (initialFilters: AdminRefundFilters = {}) => {
  const [filters, setFilters] = useState<AdminRefundFilters>(initialFilters);
  const { params, setPage, setSort } = usePagination(
    {},
    DEFAULT_PAGE_SIZE_ADMIN,
  );
  const {
    execute,
    loading: apiLoading,
    data,
  } = useApi<PageResponse<AdminRefundListResponse>>();

  const executeRef = useRef(execute);
  executeRef.current = execute;

  const loading = useDelayedLoading(apiLoading, {
    delay: 200,
    minDisplayTime: 300,
  });

  const refresh = useCallback(() => {
    return executeRef.current(() =>
      refundApi.admin.getAll({ ...params, ...filters }),
    );
  }, [params, filters]);

  const applyFilters = useCallback(
    (changes: Partial<AdminRefundFilters>) => {
      setFilters((prev) => ({ ...prev, ...changes }));
      setPage(0);
    },
    [setPage],
  );

  const clearFilters = useCallback(() => {
    setFilters({});
    setPage(0);
  }, [setPage]);

  return {
    refunds: data?.content || [],
    pagination: data,
    loading,
    filters,
    sort: params.sort,
    setPage,
    setSort,
    applyFilters,
    clearFilters,
    refresh,
  };
};
