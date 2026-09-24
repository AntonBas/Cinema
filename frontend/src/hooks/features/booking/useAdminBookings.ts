import { useCallback, useRef } from "react";
import { useApi } from "@/hooks/common/useApi";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";
import { bookingApi } from "@/api/bookingApi";
import { DEFAULT_PAGE_SIZE_ADMIN } from "@/utils/paginationUtils";
import type {
  AdminBookingFilters,
  AdminBookingListResponse,
} from "@/types/booking";
import type { PageResponse } from "@/types/pagination";

interface AdminBookingsQuery {
  filters: AdminBookingFilters;
  page: number;
  sort?: string;
}

export const useAdminBookings = ({
  filters,
  page,
  sort,
}: AdminBookingsQuery) => {
  const {
    execute,
    loading: apiLoading,
    data,
  } = useApi<PageResponse<AdminBookingListResponse>>();

  const executeRef = useRef(execute);
  executeRef.current = execute;

  const loading = useDelayedLoading(apiLoading, {
    delay: 200,
    minDisplayTime: 300,
  });

  const refresh = useCallback(() => {
    return executeRef.current(() =>
      bookingApi.admin.getAll({
        page,
        size: DEFAULT_PAGE_SIZE_ADMIN,
        sort,
        ...filters,
      }),
    );
  }, [page, sort, filters]);

  return {
    bookings: data?.content || [],
    pagination: data,
    loading,
    refresh,
  };
};
