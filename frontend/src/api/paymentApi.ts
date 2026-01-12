import type {
    PaymentResponse,
    PaymentCreateRequest,
    LiqPayCallbackRequest,
    PaymentLiqPayDataResponse,
} from '@/types/payment';
import { handleApiError } from '@/utils/apiErrorHandler';

const BASE_URL = '/api/payments';

const getAuthHeaders = (): HeadersInit => {
    const token = localStorage.getItem('authToken');
    return {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
    };
};

const fetchApi = async <T>(url: string, options: RequestInit = {}): Promise<T> => {
    const response = await fetch(url, {
        headers: getAuthHeaders(),
        ...options,
    });
    if (!response.ok) throw await handleApiError(response);
    if (response.status === 204) return undefined as T;
    return response.json();
};

export const paymentApi = {
    create: (request: PaymentCreateRequest) =>
        fetchApi<PaymentResponse>(BASE_URL, {
            method: 'POST',
            body: JSON.stringify(request),
        }),

    processLiqPayCallback: (callbackRequest: LiqPayCallbackRequest) =>
        fetchApi<string>(`${BASE_URL}/liqpay/callback`, {
            method: 'POST',
            body: JSON.stringify(callbackRequest),
        }),

    getLiqPayData: (paymentId: number) =>
        fetchApi<PaymentLiqPayDataResponse>(`${BASE_URL}/${paymentId}/liqpay-data`),

    getStatus: (paymentId: number) =>
        fetchApi<PaymentResponse>(`${BASE_URL}/${paymentId}/status`),

    retry: (paymentId: number) =>
        fetchApi<PaymentResponse>(`${BASE_URL}/${paymentId}/retry`, {
            method: 'POST',
        }),

    getByBooking: (bookingId: number) =>
        fetchApi<PaymentResponse>(`${BASE_URL}/booking/${bookingId}`)
};