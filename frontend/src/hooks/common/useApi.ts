import { useState, useCallback, useEffect, useRef } from "react";
import { useNotification } from './useNotification';

export const useApi = <T>() => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<Error | null>(null);
    const [data, setData] = useState<T | null>(null);
    const { showNotification } = useNotification();
    const abortControllerRef = useRef<AbortController | null>(null);

    const callApi = useCallback(async (
        apiCall: () => Promise<T>,
        options?: {
            showErrorNotification?: boolean;
            successMessage?: string;
            silent?: boolean;
        }
    ): Promise<T> => {
        const {
            showErrorNotification = true,
            successMessage,
            silent = false
        } = options || {};

        if (abortControllerRef.current) {
            abortControllerRef.current.abort();
        }

        abortControllerRef.current = new AbortController();

        if (!silent) setLoading(true);
        setError(null);

        try {
            const result = await apiCall();
            setData(result);

            if (successMessage && !silent) {
                showNotification(successMessage, 'success');
            }

            return result;
        } catch (err) {
            if (err instanceof DOMException && err.name === 'AbortError') {
                throw err;
            }

            const error = err instanceof Error ? err : new Error('Operation failed');
            setError(error);

            if (showErrorNotification && !silent) {
                showNotification(error.message, 'error');
            }

            throw err;
        } finally {
            if (!silent) {
                setLoading(false);
            }
            abortControllerRef.current = null;
        }
    }, [showNotification]);

    const reset = useCallback(() => {
        setLoading(false);
        setError(null);
        setData(null);
        if (abortControllerRef.current) {
            abortControllerRef.current.abort();
            abortControllerRef.current = null;
        }
    }, []);

    useEffect(() => {
        return () => {
            if (abortControllerRef.current) {
                abortControllerRef.current.abort();
            }
        };
    }, []);

    return {
        loading,
        error,
        data,
        callApi,
        reset
    };
};