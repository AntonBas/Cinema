import { useCallback, useRef } from "react";
import { refundApi } from "@/api/refundApi";
import type {
  RefundResponse,
  RefundRequest,
  RefundPolicy,
  RefundPreviewRequest,
  RefundPreviewResponse,
} from "@/types/refund";
import { useApi } from "@/hooks/common/useApi";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";

export const useRefund = () => {
  const refundApiHook = useApi<RefundResponse>();
  const policyApiHook = useApi<RefundPolicy>();
  const previewApiHook = useApi<RefundPreviewResponse>();
  const refundApiRef = useRef(refundApiHook);
  const policyApiRef = useRef(policyApiHook);
  const previewApiRef = useRef(previewApiHook);
  refundApiRef.current = refundApiHook;
  policyApiRef.current = policyApiHook;
  previewApiRef.current = previewApiHook;

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

  const getPreview = useCallback(async (request: RefundPreviewRequest) => {
    return previewApiRef.current.execute(() => refundApi.preview(request), {
      showErrorNotification: false,
    });
  }, []);

  return {
    refundResult: refundApiHook.data,
    policy: policyApiHook.data,
    previewResult: previewApiHook.data,
    previewLoading: previewApiHook.loading,
    loading,
    error: refundApiHook.error,
    processRefund,
    getPolicy,
    getPreview,
    reset: refundApiHook.reset,
  };
};
