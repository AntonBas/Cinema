import { useCallback, useRef } from "react";
import { refundApi } from "@/api/refundApi";
import type {
  RefundResponse,
  RefundRequest,
  RefundPolicy,
} from "@/types/refund";
import { useApi } from "@/hooks/common/useApi";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";

export const useRefund = () => {
  const refundApiHook = useApi<RefundResponse>();
  const policyApiHook = useApi<RefundPolicy>();
  const refundApiRef = useRef(refundApiHook);
  const policyApiRef = useRef(policyApiHook);
  refundApiRef.current = refundApiHook;
  policyApiRef.current = policyApiHook;

  const loading = useDelayedLoading(
    refundApiHook.loading || policyApiHook.loading,
    { delay: 150, minDisplayTime: 300 },
  );

  const processRefund = useCallback(async (request: RefundRequest) => {
    return refundApiRef.current.execute(
      () => refundApi.processRefund(request),
      { successMessage: "Refund request submitted successfully" },
    );
  }, []);

  const getPolicy = useCallback(async () => {
    return policyApiRef.current.execute(() => refundApi.getPolicy());
  }, []);

  return {
    refundResult: refundApiHook.data,
    policy: policyApiHook.data,
    loading,
    error: refundApiHook.error,
    processRefund,
    getPolicy,
    reset: refundApiHook.reset,
  };
};
