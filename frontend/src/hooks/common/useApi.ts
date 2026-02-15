import { useState, useCallback, useRef, useEffect } from "react";
import { useNotification } from './useNotification';

interface CacheItem<T> {
    data: T;
    timestamp: number;
    expiresAt: number;
}

interface UseApiState<T> {
    data: T | null;
    loading: boolean;
    error: Error | null;
    isCached: boolean;
    timestamp: number | null;
}

interface UseApiOptions<T> {
    showErrorNotification?: boolean;
    successMessage?: string;
    cacheKey?: string;
    cacheTime?: number;
    onSuccess?: (data: T) => void;
    onError?: (error: Error) => void;
    enabled?: boolean;
}

export const useApi = <T = any>() => {
    const [state, setState] = useState<UseApiState<T>>({
        data: null,
        loading: false,
        error: null,
        isCached: false,
        timestamp: null
    });

    const { showNotification } = useNotification();
    const abortControllerRef = useRef<AbortController | null>(null);
    const cacheRef = useRef<Map<string, CacheItem<T>>>(new Map());
    const mountedRef = useRef(true);

    useEffect(() => {
        mountedRef.current = true;
        return () => {
            mountedRef.current = false;
            if (abortControllerRef.current) {
                abortControllerRef.current.abort();
            }
        };
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

    const execute = useCallback(async <R>(
        apiCall: (signal?: AbortSignal) => Promise<R>,
        options?: UseApiOptions<R>
    ): Promise<R> => {
        const {
            showErrorNotification = true,
            successMessage,
            cacheKey,
            cacheTime = 5 * 60 * 1000,
            onSuccess,
            onError
        } = options || {};

        if (abortControllerRef.current) {
            abortControllerRef.current.abort();
        }

        abortControllerRef.current = new AbortController();

        if (cacheKey) {
            const cachedData = getCache(cacheKey) as R | null;
            if (cachedData !== null) {
                if (mountedRef.current) {
                    setState({
                        data: cachedData as unknown as T,
                        loading: false,
                        error: null,
                        isCached: true,
                        timestamp: Date.now()
                    });
                }
                if (onSuccess) onSuccess(cachedData);
                return cachedData;
            }
        }

        if (mountedRef.current) {
            setState(prev => ({
                ...prev,
                loading: true,
                error: null,
                isCached: false,
                timestamp: Date.now()
            }));
        }

        try {
            const result = await apiCall(abortControllerRef.current.signal);

            if (mountedRef.current) {
                setState({
                    data: result as unknown as T,
                    loading: false,
                    error: null,
                    isCached: false,
                    timestamp: Date.now()
                });
            }

            if (cacheKey && cacheTime > 0) {
                setCache(cacheKey, result as unknown as T, cacheTime);
            }

            if (successMessage) {
                showNotification(successMessage, 'success');
            }

            if (onSuccess) onSuccess(result);
            return result;
        } catch (err) {
            if (!mountedRef.current) throw err;

            if (err instanceof DOMException && err.name === 'AbortError') {
                return null as R;
            }

            const error = err instanceof Error ? err : new Error('Operation failed');

            setState(prev => ({
                ...prev,
                loading: false,
                error,
                isCached: false
            }));

            if (showErrorNotification) {
                showNotification(error.message, 'error');
            }

            if (onError) onError(error);
            throw error;
        } finally {
            abortControllerRef.current = null;
        }
    }, [getCache, setCache, showNotification]);

    const reset = useCallback(() => {
        if (mountedRef.current) {
            setState({
                data: null,
                loading: false,
                error: null,
                isCached: false,
                timestamp: null
            });
        }
        if (abortControllerRef.current) {
            abortControllerRef.current.abort();
            abortControllerRef.current = null;
        }
    }, []);

    return {
        ...state,
        execute,
        reset,
        invalidateCache,
        setData: (data: T | null) => {
            if (mountedRef.current) {
                setState(prev => ({ ...prev, data }));
            }
        }
    };
};