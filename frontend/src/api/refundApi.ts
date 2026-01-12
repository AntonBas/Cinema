import type {
    RefundPreviewResponse,
    RefundResponse,
    RefundPreviewRequest,
    RefundRequest,
} from '@/types/refund';
import { handleApiError } from '@/utils/apiErrorHandler';

const BASE_URL = '/api/refunds';

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

export const refundApi = {
    getPreview: (request: RefundPreviewRequest) =>
        fetchApi<RefundPreviewResponse>(`${BASE_URL}/preview`, {
            method: 'POST',
            body: JSON.stringify(request),
        }),

    processRefund: (request: RefundRequest) =>
        fetchApi<RefundResponse>(BASE_URL, {
            method: 'POST',
            body: JSON.stringify(request),
        }),

    getUserRefunds: () => fetchApi<RefundResponse[]>(BASE_URL)
};