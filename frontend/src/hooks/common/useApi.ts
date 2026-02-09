import { useState, useCallback, useEffect, useRef } from "react";
import { useNotification } from './useNotification';

interface UseApiOptions<T = any> {
    showErrorNotification?: boolean;
    successMessage?: string;
    silent?: boolean;
    cacheKey?: string;
    cacheTime?: number;
    retryCount?: number;
    retryDelay?: number;
    optimisticData?: T;
    onSuccess?: (data: T) => void;
    onError?: (error: Error) => void;
    enabled?: boolean;
}

interface CacheItem<T = any> {
    data: T;
    timestamp: number;
    expiresAt: number;
}

interface RequestState {
    isLoading: boolean;
    isError: boolean;
    isSuccess: boolean;
    isCached: boolean;
    attempts: number;
    timestamp: number | null;
}

export const useApi = <T = any>() => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<Error | null>(null);
    const [data, setData] = useState<T | null>(null);
    const [state, setState] = useState<RequestState>({
        isLoading: false,
        isError: false,
        isSuccess: false,
        isCached: false,
        attempts: 0,
        timestamp: null
    });

    const { showNotification } = useNotification();
    const abortControllerRef = useRef<AbortController | null>(null);
    const cacheRef = useRef<Map<string, CacheItem<T>>>(new Map());
    const retryTimeoutRef = useRef<NodeJS.Timeout | null>(null);
    const originalDataRef = useRef<T | null>(null);
    const lastRequestRef = useRef<{
        apiCall: (() => Promise<T>) | null;
        options?: UseApiOptions<T>;
    }>({ apiCall: null, options: undefined });

    const clearRetryTimeout = useCallback(() => {
        if (retryTimeoutRef.current) {
            clearTimeout(retryTimeoutRef.current);
            retryTimeoutRef.current = null;
        }
    }, []);

    const getCache = useCallback((key: string): T | null => {
        const item = cacheRef.current.get(key);
        if (!item) return null;

        if (Date.now() > item.expiresAt) {
            cacheRef.current.delete(key);
            return null;
        }

        return item.data;
    }, []);

    const setCache = useCallback((key: string, data: T, cacheTime: number) => {
        const now = Date.now();
        cacheRef.current.set(key, {
            data,
            timestamp: now,
            expiresAt: now + cacheTime
        });
    }, []);

    const invalidateCache = useCallback((key?: string) => {
        if (key) {
            cacheRef.current.delete(key);
        } else {
            cacheRef.current.clear();
        }
    }, []);

    const executeWithRetry = useCallback(async (
        apiCall: () => Promise<T>,
        retryCount: number,
        retryDelay: number,
        attempt: number = 1
    ): Promise<T> => {
        try {
            const result = await apiCall();
            return result;
        } catch (err) {
            if (attempt >= retryCount) {
                throw err;
            }

            if (err instanceof DOMException && err.name === 'AbortError') {
                throw err;
            }

            await new Promise(resolve => {
                retryTimeoutRef.current = setTimeout(resolve, retryDelay);
            });

            return executeWithRetry(apiCall, retryCount, retryDelay, attempt + 1);
        }
    }, []);

    const callApi = useCallback(async (
        apiCall: () => Promise<T>,
        options?: UseApiOptions<T>
    ): Promise<T> => {
        const {
            showErrorNotification = true,
            successMessage,
            silent = false,
            cacheKey,
            cacheTime = 300000,
            retryCount = 0,
            retryDelay = 1000,
            optimisticData,
            onSuccess,
            onError,
            enabled = true
        } = options || {};

        if (!enabled) {
            throw new Error('API call is disabled');
        }

        clearRetryTimeout();

        lastRequestRef.current = { apiCall, options };

        if (abortControllerRef.current) {
            abortControllerRef.current.abort();
        }

        abortControllerRef.current = new AbortController();

        setState(prev => ({
            ...prev,
            isLoading: true,
            isError: false,
            isSuccess: false,
            isCached: false,
            attempts: 0,
            timestamp: Date.now()
        }));

        if (!silent) setLoading(true);
        setError(null);

        if (cacheKey) {
            const cachedData = getCache(cacheKey);
            if (cachedData !== null) {
                setData(cachedData);
                setState(prev => ({
                    ...prev,
                    isLoading: false,
                    isSuccess: true,
                    isCached: true
                }));
                if (!silent) setLoading(false);
                if (onSuccess) onSuccess(cachedData);
                return cachedData;
            }
        }

        if (optimisticData !== undefined) {
            originalDataRef.current = data;
            setData(optimisticData);
        }

        try {
            let result: T;

            if (retryCount > 0) {
                result = await executeWithRetry(
                    apiCall,
                    retryCount,
                    retryDelay
                );
            } else {
                result = await apiCall();
            }

            setData(result);
            setState(prev => ({
                ...prev,
                isLoading: false,
                isSuccess: true,
                isError: false,
                attempts: prev.attempts + 1
            }));

            if (cacheKey && cacheTime > 0) {
                setCache(cacheKey, result, cacheTime);
            }

            if (successMessage && !silent) {
                showNotification(successMessage, 'success');
            }

            if (onSuccess) onSuccess(result);
            return result;
        } catch (err) {
            if (err instanceof DOMException && err.name === 'AbortError') {
                throw err;
            }

            const error = err instanceof Error ? err : new Error('Operation failed');
            setError(error);

            setState(prev => ({
                ...prev,
                isLoading: false,
                isError: true,
                isSuccess: false,
                attempts: prev.attempts + 1
            }));

            if (optimisticData !== undefined && originalDataRef.current !== null) {
                setData(originalDataRef.current);
            }

            if (showErrorNotification && !silent) {
                showNotification(error.message, 'error');
            }

            if (onError) onError(error);
            throw err;
        } finally {
            if (!silent) {
                setLoading(false);
            }
            abortControllerRef.current = null;
        }
    }, [showNotification, getCache, setCache, executeWithRetry, clearRetryTimeout]);

    const reset = useCallback(() => {
        setLoading(false);
        setError(null);
        setData(null);
        setState({
            isLoading: false,
            isError: false,
            isSuccess: false,
            isCached: false,
            attempts: 0,
            timestamp: null
        });
        clearRetryTimeout();

        if (abortControllerRef.current) {
            abortControllerRef.current.abort();
            abortControllerRef.current = null;
        }
    }, [clearRetryTimeout]);

    const refetch = useCallback(async (): Promise<T | null> => {
        if (!lastRequestRef.current.apiCall) return null;

        try {
            return await callApi(
                lastRequestRef.current.apiCall,
                lastRequestRef.current.options
            );
        } catch {
            return null;
        }
    }, [callApi]);

    const updateData = useCallback((updater: (currentData: T | null) => T | null) => {
        setData(prev => updater(prev));
    }, []);

    useEffect(() => {
        return () => {
            clearRetryTimeout();
            if (abortControllerRef.current) {
                abortControllerRef.current.abort();
            }
        };
    }, [clearRetryTimeout]);

    return {
        loading,
        error,
        data,
        state,
        callApi,
        reset,
        refetch,
        updateData,
        invalidateCache,
        setData,
        setError,
        setLoading: (isLoading: boolean) => {
            setLoading(isLoading);
            setState(prev => ({ ...prev, isLoading }));
        },
        isIdle: !state.isLoading && !state.isError && !state.isSuccess,
        isCached: state.isCached
    };
};