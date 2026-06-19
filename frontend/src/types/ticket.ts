export type TicketStatus = "ACTIVE" | "USED" | "REFUNDED" | "EXPIRED";

export interface TicketFilterRequest {
  status?: TicketStatus;
  movieTitle?: string;
}

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
  row?: number;
  seatNumber?: number;
}

export interface TicketCashierResponse {
  id: number;
  uniqueCode: string;
  status: TicketStatus;
  movieTitle: string;
  sessionTime: string;
  hallName: string;
  seatRow: string;
  seatNumber: number;
  ticketType: string;
  requiresDocument: boolean;
  documentType: string | null;
  userEmail: string;
  finalPrice: string;
}

export const TicketStatusDisplay: Record<TicketStatus, string> = {
  ACTIVE: "Active",
  USED: "Used",
  REFUNDED: "Refunded",
  EXPIRED: "Expired",
};

export const TicketStatusColor: Record<TicketStatus, string> = {
  ACTIVE: "green",
  USED: "blue",
  REFUNDED: "gray",
  EXPIRED: "orange",
};
