import { useCallback, useRef } from "react";
import { ticketApi } from "@/api/ticketApi";
import type {
  TicketResponse,
  TicketCashierResponse,
  TicketFilterRequest,
} from "@/types/ticket";
import type { PageResponse, SearchParams } from "@/types/pagination";
import { useApi } from "@/hooks/common/useApi";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";

export const useTickets = () => {
  const ticketsApi = useApi<PageResponse<TicketResponse>>();
  const ticketApiHook = useApi<TicketResponse>();
  const qrCodeApi = useApi<Blob>();
  const validateApi = useApi<void>();
  const cashierTicketApi = useApi<TicketCashierResponse>();
  const cashierValidateApi = useApi<TicketCashierResponse>();

  const ticketsApiRef = useRef(ticketsApi);
  const ticketApiRef = useRef(ticketApiHook);
  const qrCodeApiRef = useRef(qrCodeApi);
  const validateApiRef = useRef(validateApi);
  const cashierTicketApiRef = useRef(cashierTicketApi);
  const cashierValidateApiRef = useRef(cashierValidateApi);

  ticketsApiRef.current = ticketsApi;
  ticketApiRef.current = ticketApiHook;
  qrCodeApiRef.current = qrCodeApi;
  validateApiRef.current = validateApi;
  cashierTicketApiRef.current = cashierTicketApi;
  cashierValidateApiRef.current = cashierValidateApi;

  const loading = useDelayedLoading(
    ticketsApi.loading ||
      ticketApiHook.loading ||
      qrCodeApi.loading ||
      validateApi.loading ||
      cashierTicketApi.loading ||
      cashierValidateApi.loading,
    { delay: 150, minDisplayTime: 300 },
  );

  const getUserTickets = useCallback(
    async (params?: SearchParams & TicketFilterRequest) => {
      return ticketsApiRef.current.execute(() =>
        ticketApi.getUserTickets(params),
      );
    },
    [],
  );

  const getByCode = useCallback(async (ticketCode: string) => {
    return ticketApiRef.current.execute(() => ticketApi.getByCode(ticketCode));
  }, []);

  const getQRCode = useCallback(async (ticketCode: string) => {
    return qrCodeApiRef.current.execute(() => ticketApi.getQRCode(ticketCode));
  }, []);

  const validate = useCallback(async (ticketCode: string) => {
    return validateApiRef.current.execute(
      () => ticketApi.validate(ticketCode),
      { successMessage: "Ticket validated successfully" },
    );
  }, []);

  const getTicketForCashier = useCallback(async (uniqueCode: string) => {
    return cashierTicketApiRef.current.execute(() =>
      ticketApi.getTicketForCashier(uniqueCode),
    );
  }, []);

  const validateTicket = useCallback(async (uniqueCode: string) => {
    return cashierValidateApiRef.current.execute(
      () => ticketApi.validateTicket(uniqueCode),
      { successMessage: "Ticket validated successfully" },
    );
  }, []);

  return {
    tickets: ticketsApi.data?.content || [],
    pagination: ticketsApi.data,
    ticket: ticketApiHook.data,
    qrCode: qrCodeApi.data,
    cashierTicket: cashierTicketApi.data,
    validatedTicket: cashierValidateApi.data,
    loading,
    ticketsError: ticketsApi.error,
    ticketError: ticketApiHook.error,
    qrCodeError: qrCodeApi.error,
    validateError: validateApi.error,
    cashierTicketError: cashierTicketApi.error,
    cashierValidateError: cashierValidateApi.error,
    getUserTickets,
    getByCode,
    getQRCode,
    validate,
    getTicketForCashier,
    validateTicket,
  };
};
