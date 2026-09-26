import { useCallback, useRef } from "react";
import { useApi } from "@/hooks/common/useApi";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";
import { refundApi } from "@/api/refundApi";
import { DEFAULT_PAGE_SIZE_ADMIN } from "@/utils/paginationUtils";
import type {
  AdminRefundFilters,
  AdminRefundListResponse,
} from "@/types/refund";
import type { PageResponse } from "@/types/pagination";

interface AdminRefundsQuery {
  filters: AdminRefundFilters;
  page: number;
  sort?: string;
}

export const useAdminRefunds = ({ filters, page, sort }: AdminRefundsQuery) => {
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
      refundApi.admin.getAll({
        page,
        size: DEFAULT_PAGE_SIZE_ADMIN,
        sort,
        ...filters,
      }),
    );
  }, [page, sort, filters]);

  return {
    refunds: data?.content || [],
    pagination: data,
    loading,
    refresh,
  };
};
