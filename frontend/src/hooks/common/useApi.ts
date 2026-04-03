import { useState, useCallback, useRef, useEffect } from "react";
import { useNotification } from './useNotification';
import { isApiErrorException, ApiErrorException } from '@/utils/apiErrorHandler';
import type { AxiosResponse } from 'axios';

interface UseApiState<T> {
    data: T | null;
    loading: boolean;
    error: Error | ApiErrorException | null;
    timestamp: number | null;
}

interface UseApiOptions<T> {
    showErrorNotification?: boolean;
    successMessage?: string;
    onSuccess?: (data: T) => void;
    onError?: (error: Error | ApiErrorException) => void;
}

export const useApi = <T = any>() => {
    const [state, setState] = useState<UseApiState<T>>({
        data: null,
        loading: false,
        error: null,
        timestamp: null
    });

    const { showNotification } = useNotification();
    const abortControllerRef = useRef<AbortController | null>(null);
    const mountedRef = useRef(true);
    const loadingRef = useRef(false);

    useEffect(() => {
        mountedRef.current = true;
        return () => {
            mountedRef.current = false;
            if (abortControllerRef.current) {
                abortControllerRef.current.abort();
            }
        };
    }, []);

    const getErrorMessage = useCallback((error: Error | ApiErrorException): string => {
        if (isApiErrorException(error)) {
            return error.message;
        }
        return error.message || 'Operation failed';
    }, []);

    const execute = useCallback(async <R>(
        apiCall: (signal?: AbortSignal) => Promise<AxiosResponse<R>>,
        options?: UseApiOptions<R>
    ): Promise<R | null> => {
        if (loadingRef.current) {
            return null;
        }

        const {
            showErrorNotification = true,
            successMessage,
            onSuccess,
            onError
        } = options || {};

        if (abortControllerRef.current) {
            abortControllerRef.current.abort();
        }

        abortControllerRef.current = new AbortController();

        if (mountedRef.current) {
            setState(prev => ({
                ...prev,
                loading: true,
                error: null,
                timestamp: Date.now()
            }));
        }

        loadingRef.current = true;

        try {
            const response = await apiCall(abortControllerRef.current.signal);
            const responseData = response.data;

            if (mountedRef.current) {
                setState({
                    data: responseData as unknown as T,
                    loading: false,
                    error: null,
                    timestamp: Date.now()
                });
            }

            if (successMessage) {
                showNotification(successMessage, 'success');
            }

            if (onSuccess) {
                onSuccess(responseData);
            }

            return responseData;
        } catch (err) {
            if (err instanceof DOMException && err.name === 'AbortError') {
                return null;
            }

            const error = err instanceof Error ? err : new Error('Operation failed');

            if (mountedRef.current) {
                setState(prev => ({
                    ...prev,
                    loading: false,
                    error,
                }));
            }

            if (showErrorNotification) {
                const errorMessage = getErrorMessage(error);
                showNotification(errorMessage, 'error');
            }

            if (onError) onError(error);
            throw error;
        } finally {
            loadingRef.current = false;
            abortControllerRef.current = null;
        }
    }, [showNotification, getErrorMessage]);

    const reset = useCallback(() => {
        if (mountedRef.current) {
            setState({
                data: null,
                loading: false,
                error: null,
                timestamp: null
            });
        }
        loadingRef.current = false;
        if (abortControllerRef.current) {
            abortControllerRef.current.abort();
            abortControllerRef.current = null;
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
                setState(prev => ({ ...prev, data }));
            }
        }
    };
};