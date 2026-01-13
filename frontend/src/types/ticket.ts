export type TicketStatus = 'PENDING' | 'ACTIVE' | 'USED' | 'CANCELLED' | 'REFUNDED' | 'EXPIRED';

export interface TicketResponse {
    id: number;
    ticketCode: string;
    qrCodeUrl: string;
    status: TicketStatus;
    purchaseTime: string;
    price: string;
    ticketType: string;
    movieTitle: string;
    sessionTime: string;
    hallName: string;
    row: number;
    seatNumber: number;
}

export const TicketStatusDisplay: Record<TicketStatus, string> = {
    PENDING: 'Pending',
    ACTIVE: 'Active',
    USED: 'Used',
    CANCELLED: 'Cancelled',
    REFUNDED: 'Refunded',
    EXPIRED: 'Expired'
};