import { useCallback, useRef } from "react";
import { paymentApi } from "@/api/paymentApi";
import type {
  PaymentResponse,
  PaymentCreateRequest,
  PaymentLiqPayDataResponse,
} from "@/types/payment";
import { useApi } from "@/hooks/common/useApi";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";

export const usePayment = () => {
  const paymentApiHook = useApi<PaymentResponse>();
  const liqPayDataApi = useApi<PaymentLiqPayDataResponse>();
  const mutationApi = useApi<PaymentResponse>();

  const paymentApiRef = useRef(paymentApiHook);
  const liqPayDataApiRef = useRef(liqPayDataApi);
  const mutationApiRef = useRef(mutationApi);

  paymentApiRef.current = paymentApiHook;
  liqPayDataApiRef.current = liqPayDataApi;
  mutationApiRef.current = mutationApi;

  const loading = useDelayedLoading(
    paymentApiHook.loading || liqPayDataApi.loading || mutationApi.loading,
    { delay: 150, minDisplayTime: 300 },
  );

  const create = useCallback(async (request: PaymentCreateRequest) => {
    return mutationApiRef.current.execute(() => paymentApi.create(request), {
      successMessage: "Payment initialized successfully",
    });
  }, []);

  const getById = useCallback(async (paymentId: number) => {
    return paymentApiRef.current.execute(() => paymentApi.getById(paymentId));
  }, []);

  const getLiqPayData = useCallback(async (paymentId: number) => {
    return liqPayDataApiRef.current.execute(() =>
      paymentApi.getLiqPayData(paymentId),
    );
  }, []);

  const retry = useCallback(async (paymentId: number) => {
    return mutationApiRef.current.execute(() => paymentApi.retry(paymentId), {
      successMessage: "Payment retry initiated",
    });
  }, []);

  return {
    payment: paymentApiHook.data,
    liqPayData: liqPayDataApi.data,
    loading,
    paymentError: paymentApiHook.error,
    liqPayError: liqPayDataApi.error,
    mutationError: mutationApi.error,
    create,
    getById,
    getLiqPayData,
    retry,
  };
};
