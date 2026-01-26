import { useState, useCallback } from 'react';
import { paymentApi } from '@/api/paymentApi';
import type {
    PaymentResponse,
    PaymentCreateRequest,
    PaymentLiqPayDataResponse,
} from '@/types/payment';
import { isApiErrorException } from '@/utils/apiErrorHandler';

export const usePayment = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const create = useCallback(async (request: PaymentCreateRequest): Promise<PaymentResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await paymentApi.create(request);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to create payment';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getById = useCallback(async (paymentId: number): Promise<PaymentResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await paymentApi.getById(paymentId);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : `Failed to get payment for ID: ${paymentId}`;
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getLiqPayData = useCallback(async (paymentId: number): Promise<PaymentLiqPayDataResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await paymentApi.getLiqPayData(paymentId);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : `Failed to get payment data for ID: ${paymentId}`;
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getStatus = useCallback(async (paymentId: number): Promise<PaymentResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await paymentApi.getStatus(paymentId);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : `Failed to get payment status for ID: ${paymentId}`;
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const retry = useCallback(async (paymentId: number): Promise<PaymentResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await paymentApi.retry(paymentId);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : `Failed to retry payment for ID: ${paymentId}`;
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getByBooking = useCallback(async (bookingId: number): Promise<PaymentResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await paymentApi.getByBooking(bookingId);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : `Failed to get payment for booking ID: ${bookingId}`;
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const clearError = useCallback(() => {
        setError(null);
    }, []);

    return {
        loading,
        error,
        create,
        getById,
        getLiqPayData,
        getStatus,
        retry,
        getByBooking,
        clearError
    };
};