export enum SeatType {
  STANDARD = "STANDARD",
  VIP = "VIP",
  COUPLE = "COUPLE",
}

export interface SeatResponse {
  id: number;
  row: number;
  number: number;
  seatType: SeatType;
  x: number;
  y: number;
  active: boolean;
}

export interface SeatRowResponse {
  rowNumber: number;
  seatsCount: number;
  seats: SeatResponse[];
}
