import { api } from '@/services/api';
import type {
    RefundResponse,
    RefundRequest,
} from '@/types/refund';

const BASE_URL = '/api/refunds';

export const refundApi = {
    processRefund: (request: RefundRequest) =>
        api.post<RefundResponse>(BASE_URL, request),
};