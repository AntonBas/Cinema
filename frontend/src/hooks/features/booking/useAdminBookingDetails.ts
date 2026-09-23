import { useCallback, useRef } from "react";
import { useApi } from "@/hooks/common/useApi";
import { bookingApi } from "@/api/bookingApi";
import type { AdminBookingDetailsResponse } from "@/types/booking";

export const useAdminBookingDetails = () => {
  const { data, loading, error, execute } =
    useApi<AdminBookingDetailsResponse>();
  const executeRef = useRef(execute);
  executeRef.current = execute;

  const getBooking = useCallback(async (bookingId: number) => {
    return executeRef.current(() => bookingApi.admin.getById(bookingId));
  }, []);

  return { booking: data, loading, error, getBooking };
};
