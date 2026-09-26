import type { PaymentStatus } from "@/types/payment";
import type { TicketStatus } from "@/types/ticket";

export type BookingStatus = "PENDING" | "CONFIRMED" | "CANCELLED" | "EXPIRED";

export interface BookingCreateRequest {
  sessionId: string;
  seats: SeatSelectionRequest[];
  bonusPointsToUse?: number;
}

export interface SeatSelectionRequest {
  seatId: number;
  ticketTypeId: number;
}

export interface BookingResponse {
  id: number;
  publicId: string;
  bookingNumber: string;
  status: BookingStatus;
  sessionId: number;
  sessionPublicId: string;
  sessionTime: string;
  movieTitle: string;
  hallName: string;
  totalPrice: string;
  bonusPointsUsed: number;
  bonusDiscountAmount: string;
  finalPrice: string;
  liqpayOrderId?: string;
  expiresAt: string;
  seatReservations: SeatReservationInfo[];
}

export interface SeatReservationInfo {
  id: number;
  seatId: number;
  row: number;
  seatNumber: number;
  ticketTypeName: string;
  seatPrice: string;
}

export interface BookingTempHoldResponse {
  bookingId: number;
  expiresAt: string;
  remainingSeconds: number;
}

export const BookingStatusDisplay: Record<BookingStatus, string> = {
  PENDING: "Pending",
  CONFIRMED: "Confirmed",
  CANCELLED: "Cancelled",
  EXPIRED: "Expired",
};

export interface AdminBookingListResponse {
  id: number;
  publicId: string;
  bookingNumber: string;
  status: BookingStatus;
  createdDate: string;
  userId: number;
  userEmail: string;
  movieTitle: string;
  hallName: string;
  sessionTime: string;
  finalPrice: string;
  paymentStatus?: PaymentStatus;
}

export interface AdminBookingTicketInfo {
  id: number;
  ticketCode: string;
  status: TicketStatus;
  ticketType: string;
  row: number;
  seatNumber: number;
  price: string;
}

export interface AdminBookingPaymentInfo {
  id: number;
  status: PaymentStatus;
  amount: string;
  paymentTime?: string;
  liqpayOrderId?: string;
  cardMask?: string;
  errorCode?: string;
  errorDescription?: string;
}

export interface AdminBookingDetailsResponse {
  id: number;
  publicId: string;
  bookingNumber: string;
  status: BookingStatus;
  createdDate: string;
  expiresAt: string;
  userId: number;
  userEmail: string;
  userFirstName: string;
  userLastName: string;
  sessionId: number;
  movieTitle: string;
  hallName: string;
  sessionTime: string;
  totalPrice: string;
  bonusPointsUsed: number;
  bonusDiscountAmount: string;
  finalPrice: string;
  seatReservations: SeatReservationInfo[];
  tickets: AdminBookingTicketInfo[];
  payment?: AdminBookingPaymentInfo;
}

export interface AdminBookingFilters {
  query?: string;
  userId?: number;
  status?: BookingStatus;
  paymentStatus?: PaymentStatus;
  dateFrom?: string;
  dateTo?: string;
}
