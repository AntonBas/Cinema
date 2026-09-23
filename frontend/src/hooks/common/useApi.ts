import { useState, useCallback, useRef, useEffect } from "react";
import { useNotification } from "@/context/NotificationContext";
import {
  isApiErrorException,
  ApiErrorException,
} from "@/utils/apiErrorHandler";
import axios, { type AxiosResponse } from "axios";

interface UseApiState<T> {
  data: T | null;
  loading: boolean;
  error: Error | ApiErrorException | null;
  timestamp: number | null;
}

export interface UseApiOptions<T> {
  showErrorNotification?: boolean;
  suppressValidationToast?: boolean;
  successMessage?: string;
  onSuccess?: (data: T) => void;
  onError?: (error: Error | ApiErrorException) => void;
  dedupeKey?: string;
}

export const useApi = <T = unknown>() => {
  const [state, setState] = useState<UseApiState<T>>({
    data: null,
    loading: false,
    error: null,
    timestamp: null,
  });

  const { showNotification } = useNotification();
  const abortControllerRef = useRef(new AbortController());
  const mountedRef = useRef(true);
  const latestRequestIdRef = useRef(0);
  const inFlightRequestsRef = useRef(new Map<string, Promise<unknown>>());

  useEffect(() => {
    mountedRef.current = true;
    if (abortControllerRef.current.signal.aborted) {
      abortControllerRef.current = new AbortController();
    }
    const abortController = abortControllerRef.current;
    return () => {
      mountedRef.current = false;
      abortController.abort();
    };
  }, []);

  const getErrorMessage = useCallback(
    (error: Error | ApiErrorException): string => {
      if (isApiErrorException(error)) {
        return error.message;
      }
      return error.message || "Operation failed";
    },
    [],
  );

  const run = useCallback(
    async <R>(
      apiCall: (signal?: AbortSignal) => Promise<AxiosResponse<R>>,
      options?: UseApiOptions<R>,
    ): Promise<R | null> => {
      const {
        showErrorNotification = true,
        suppressValidationToast = false,
        successMessage,
        onSuccess,
        onError,
      } = options || {};

      const requestId = ++latestRequestIdRef.current;
      const isLatestRequest = () =>
        mountedRef.current && requestId === latestRequestIdRef.current;

      if (mountedRef.current) {
        setState((prev) => ({
          ...prev,
          loading: true,
          error: null,
          timestamp: Date.now(),
        }));
      }

      try {
        const response = await apiCall(abortControllerRef.current.signal);
        const responseData = response.data;

        if (isLatestRequest()) {
          setState({
            data: responseData as unknown as T,
            loading: false,
            error: null,
            timestamp: Date.now(),
          });
        }

        if (successMessage) {
          showNotification(successMessage, "success");
        }

        if (onSuccess) {
          onSuccess(responseData);
        }

        return responseData;
      } catch (err) {
        if (
          axios.isCancel(err) ||
          (err instanceof DOMException && err.name === "AbortError")
        ) {
          return null;
        }

        const error =
          err instanceof Error ? err : new Error("Operation failed");

        if (isLatestRequest()) {
          setState((prev) => ({
            ...prev,
            loading: false,
            error,
          }));
        }

        const isFieldValidationError =
          isApiErrorException(error) && error.isValidationError();

        if (
          showErrorNotification &&
          !(suppressValidationToast && isFieldValidationError)
        ) {
          const errorMessage = getErrorMessage(error);
          showNotification(errorMessage, "error");
        }

        if (onError) onError(error);
        throw error;
      }
    },
    [showNotification, getErrorMessage],
  );

  const execute = useCallback(
    <R>(
      apiCall: (signal?: AbortSignal) => Promise<AxiosResponse<R>>,
      options?: UseApiOptions<R>,
    ): Promise<R | null> => {
      const dedupeKey = options?.dedupeKey;
      if (!dedupeKey) {
        return run(apiCall, options);
      }

      const inFlightRequests = inFlightRequestsRef.current;
      const inFlightRequest = inFlightRequests.get(dedupeKey);
      if (inFlightRequest) {
        return inFlightRequest as Promise<R | null>;
      }

      const request = run(apiCall, options).finally(() => {
        inFlightRequests.delete(dedupeKey);
      });
      inFlightRequests.set(dedupeKey, request);
      return request;
    },
    [run],
  );

  const reset = useCallback(() => {
    latestRequestIdRef.current += 1;
    abortControllerRef.current.abort();
    abortControllerRef.current = new AbortController();
    if (mountedRef.current) {
      setState({
        data: null,
        loading: false,
        error: null,
        timestamp: null,
      });
    }
  }, []);

  return {
    data: state.data,
    loading: state.loading,
    error: state.error,
    timestamp: state.timestamp,
    execute,
    reset,
    isApiError: isApiErrorException(state.error),
    getErrorMessage: state.error ? getErrorMessage(state.error) : null,
    setData: (data: T | null) => {
      if (mountedRef.current) {
        setState((prev) => ({ ...prev, data }));
      }
    },
    updateData: (updater: (prev: T | null) => T | null) => {
      if (mountedRef.current) {
        setState((prev) => ({ ...prev, data: updater(prev.data) }));
      }
    },
  };
};
