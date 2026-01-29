export type TicketStatus = 'ACTIVE' | 'USED' | 'REFUNDED';

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
    ACTIVE: 'Active',
    USED: 'Used',
    REFUNDED: 'Refunded',
};