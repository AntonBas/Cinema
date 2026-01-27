import type {
    PaymentResponse,
    PaymentCreateRequest,
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

    getById: (paymentId: number) =>
        fetchApi<PaymentResponse>(`${BASE_URL}/${paymentId}`),

    getLiqPayData: (paymentId: number) =>
        fetchApi<PaymentLiqPayDataResponse>(`${BASE_URL}/${paymentId}/liqpay-data`),
};