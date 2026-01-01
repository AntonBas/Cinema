import { useState, useCallback, useRef } from 'react';
import { sessionApi } from '@/api/sessionApi';
import type { SessionAdminResponse, SessionCreateRequest, SessionUpdateRequest } from '@/types/session';

interface UseSessionMutationOptions {
    onSuccess?: (data: SessionAdminResponse, operation: 'create' | 'update' | 'delete' | 'cancel' | 'reactivate') => void;
    onError?: (error: string) => void;
}

interface UseSessionMutationReturn {
    createSession: (request: SessionCreateRequest) => Promise<SessionAdminResponse>;
    updateSession: (id: number, request: SessionUpdateRequest) => Promise<SessionAdminResponse>;
    cancelSession: (id: number) => Promise<void>;
    reactivateSession: (id: number) => Promise<void>;
    deleteSession: (id: number) => Promise<void>;
    checkTimeConflict: (
        hallId: number,
        startTime: string,
        durationMinutes: number,
        excludeSessionId?: number
    ) => Promise<boolean>;
    loading: boolean;
    error: string | null;
    resetError: () => void;
}

export const useSessionMutation = (options?: UseSessionMutationOptions): UseSessionMutationReturn => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const abortControllerRef = useRef<AbortController | null>(null);

    const resetError = useCallback(() => {
        setError(null);
    }, []);

    const createSession = useCallback(async (request: SessionCreateRequest): Promise<SessionAdminResponse> => {
        setLoading(true);
        setError(null);

        try {
            const result = await sessionApi.createSession(request);
            options?.onSuccess?.(result, 'create');
            return result;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to create session';
            setError(message);
            options?.onError?.(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, [options]);

    const updateSession = useCallback(async (id: number, request: SessionUpdateRequest): Promise<SessionAdminResponse> => {
        setLoading(true);
        setError(null);

        try {
            const result = await sessionApi.updateSession(id, request);
            options?.onSuccess?.(result, 'update');
            return result;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to update session';
            setError(message);
            options?.onError?.(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, [options]);

    const cancelSession = useCallback(async (id: number): Promise<void> => {
        setLoading(true);
        setError(null);

        try {
            await sessionApi.cancelSession(id);
            options?.onSuccess?.({} as SessionAdminResponse, 'cancel');
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to cancel session';
            setError(message);
            options?.onError?.(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, [options]);

    const reactivateSession = useCallback(async (id: number): Promise<void> => {
        setLoading(true);
        setError(null);

        try {
            await sessionApi.reactivateSession(id);
            options?.onSuccess?.({} as SessionAdminResponse, 'reactivate');
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to reactivate session';
            setError(message);
            options?.onError?.(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, [options]);

    const deleteSession = useCallback(async (id: number): Promise<void> => {
        setLoading(true);
        setError(null);

        try {
            await sessionApi.deleteSession(id);
            options?.onSuccess?.({} as SessionAdminResponse, 'delete');
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to delete session';
            setError(message);
            options?.onError?.(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, [options]);

    const checkTimeConflict = useCallback(async (
        hallId: number,
        startTime: string,
        durationMinutes: number,
        excludeSessionId?: number
    ): Promise<boolean> => {
        if (abortControllerRef.current) {
            abortControllerRef.current.abort();
        }

        abortControllerRef.current = new AbortController();

        try {
            return await sessionApi.checkTimeConflict(hallId, startTime, durationMinutes, excludeSessionId);
        } catch (err) {
            if (err instanceof DOMException && err.name === 'AbortError') {
                return false;
            }
            const message = err instanceof Error ? err.message : 'Failed to check time conflict';
            setError(message);
            options?.onError?.(message);
            throw err;
        }
    }, [options]);

    return {
        createSession,
        updateSession,
        cancelSession,
        reactivateSession,
        deleteSession,
        checkTimeConflict,
        loading,
        error,
        resetError
    };
};