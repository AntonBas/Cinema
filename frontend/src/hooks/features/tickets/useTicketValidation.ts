import { useState, useCallback } from 'react';
import { ApiErrorException } from '@/utils/apiErrorHandler';

const BASE_URL = '/api/tickets';

export interface ValidationResult {
    success: boolean;
    message: string;
    ticketCode?: string;
    status?: string;
    timestamp?: string;
}

export const useTicketValidation = () => {
    const [validating, setValidating] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [validationHistory, setValidationHistory] = useState<ValidationResult[]>([]);

    const getAuthHeaders = useCallback((): HeadersInit => {
        const token = localStorage.getItem('authToken');
        return {
            'Content-Type': 'application/json',
            ...(token && { 'Authorization': `Bearer ${token}` }),
        };
    }, []);

    const fetchApi = useCallback(async <T>(url: string, options: RequestInit = {}): Promise<T> => {
        const response = await fetch(url, {
            headers: getAuthHeaders(),
            ...options,
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new ApiErrorException(errorData);
        }
        return response.json();
    }, [getAuthHeaders]);

    const validateTicket = useCallback(async (ticketCode: string) => {
        setValidating(true);
        setError(null);

        try {
            const result = await fetchApi<string>(`${BASE_URL}/validate/${ticketCode}`, {
                method: 'POST',
            });

            const validationResult: ValidationResult = {
                success: true,
                message: result,
                ticketCode,
                timestamp: new Date().toISOString()
            };

            setValidationHistory(prev => [validationResult, ...prev.slice(0, 9)]);
            return validationResult;
        } catch (err) {
            const message = err instanceof ApiErrorException ? err.message : 'Validation failed';
            setError(message);

            const validationResult: ValidationResult = {
                success: false,
                message,
                ticketCode,
                timestamp: new Date().toISOString()
            };

            setValidationHistory(prev => [validationResult, ...prev.slice(0, 9)]);
            throw err;
        } finally {
            setValidating(false);
        }
    }, [fetchApi]);

    const checkTicketStatus = useCallback(async (ticketCode: string) => {
        setValidating(true);
        setError(null);

        try {
            const status = await fetchApi<string>(`${BASE_URL}/${ticketCode}/status`);

            const validationResult: ValidationResult = {
                success: true,
                message: `Ticket status: ${status}`,
                ticketCode,
                status,
                timestamp: new Date().toISOString()
            };

            setValidationHistory(prev => [validationResult, ...prev.slice(0, 9)]);
            return validationResult;
        } catch (err) {
            const message = err instanceof ApiErrorException ? err.message : 'Status check failed';
            setError(message);

            const validationResult: ValidationResult = {
                success: false,
                message,
                ticketCode,
                timestamp: new Date().toISOString()
            };

            setValidationHistory(prev => [validationResult, ...prev.slice(0, 9)]);
            throw err;
        } finally {
            setValidating(false);
        }
    }, [fetchApi]);

    const batchValidateTickets = useCallback(async (ticketCodes: string[]) => {
        setValidating(true);
        setError(null);

        const results: ValidationResult[] = [];

        for (const ticketCode of ticketCodes) {
            try {
                const result = await fetchApi<string>(`${BASE_URL}/validate/${ticketCode}`, {
                    method: 'POST',
                });

                results.push({
                    success: true,
                    message: result,
                    ticketCode,
                    timestamp: new Date().toISOString()
                });
            } catch (err) {
                const message = err instanceof ApiErrorException ? err.message : 'Validation failed';
                results.push({
                    success: false,
                    message,
                    ticketCode,
                    timestamp: new Date().toISOString()
                });
            }
        }

        setValidating(false);
        setValidationHistory(prev => [...results, ...prev.slice(0, 10 - results.length)]);
        return results;
    }, [fetchApi]);

    const clearValidationHistory = useCallback(() => {
        setValidationHistory([]);
    }, []);

    const clearError = useCallback(() => {
        setError(null);
    }, []);

    const getRecentValidations = useCallback((limit: number = 10) => {
        return validationHistory.slice(0, limit);
    }, [validationHistory]);

    const getValidationStatistics = useCallback(() => {
        const total = validationHistory.length;
        const successful = validationHistory.filter(v => v.success).length;
        const failed = total - successful;

        return {
            total,
            successful,
            failed,
            successRate: total > 0 ? (successful / total) * 100 : 0
        };
    }, [validationHistory]);

    return {
        validating,
        error,
        validationHistory,
        validateTicket,
        checkTicketStatus,
        batchValidateTickets,
        clearValidationHistory,
        clearError,
        getRecentValidations,
        getValidationStatistics,
        hasHistory: validationHistory.length > 0
    };
};