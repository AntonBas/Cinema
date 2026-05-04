import React, { useState, useEffect } from "react";
import { Tooltip } from "@/components/ui/Tooltip/Tooltip";
import { useBonus } from "@/hooks/features/bonus/useBonus";
import { TicketTypeSelect } from "../TicketTypeSelect/TicketTypeSelect";
import styles from "./BookingSidebar.module.css";

interface SelectedSeatItem {
  seat: {
    id: number;
    row: number;
    seatNumber: number;
    ticketPrices?: Array<{
      ticketTypeId: number;
      finalPrice: string;
      ticketTypeName?: string;
      minAge?: number;
      maxAge?: number;
      requiresDocument: boolean;
      documentType?: string;
    }>;
  };
  ticketTypeId?: number;
  price: number;
}

interface BookingSidebarProps {
  selectedSeats: SelectedSeatItem[];
  totalPrice: number;
  onTicketTypeChange: (seatId: number, ticketTypeId: number) => void;
  onBooking: (bonusPointsToUse: number) => Promise<void>;
  isBooking: boolean;
}

export const BookingSidebar: React.FC<BookingSidebarProps> = ({
  selectedSeats,
  totalPrice,
  onTicketTypeChange,
  onBooking,
  isBooking,
}) => {
  const [bonusPointsToUse, setBonusPointsToUse] = useState(0);
  const { balance, getMyBalance, loading } = useBonus();

  useEffect(() => {
    getMyBalance();
  }, [getMyBalance]);

  const bonusBalance = balance?.pointsBalance || 0;
  const minUsablePoints = balance?.minUsablePoints || 0;
  const maxUsablePoints = balance?.maxUsablePoints || 0;

  const maxAvailablePoints = Math.min(
    bonusBalance,
    maxUsablePoints,
    Math.floor(totalPrice * 0.5),
  );

  const handleBonusPointsChange = (points: number) => {
    setBonusPointsToUse(Math.max(0, Math.min(points, maxAvailablePoints)));
  };

  const handleUseAllPoints = () => {
    setBonusPointsToUse(maxAvailablePoints);
  };

  const handleBooking = () => {
    onBooking(bonusPointsToUse);
  };

  const discount = bonusPointsToUse;
  const finalPrice = totalPrice - discount;

  if (!selectedSeats.length) {
    return (
      <div className={styles.sidebar}>
        <div className={styles.empty}>
          <div className={styles.emptyIcon}>🎬</div>
          <h3>No seats selected</h3>
          <p>Click on available seats to select them</p>
        </div>
      </div>
    );
  }

  const bonusRules = [
    `• 1 bonus point = 1₴ discount`,
    `• Minimum points to use: ${minUsablePoints}`,
    `• Cannot cover more than 50% of total price`,
    `• Maximum usable: ${maxAvailablePoints} points (${maxAvailablePoints.toFixed(2)}₴)`,
  ].join("\n");

  return (
    <div className={styles.sidebar}>
      <div className={styles.header}>
        <h3>Selected Seats</h3>
        <span className={styles.seatCount}>{selectedSeats.length} seats</span>
      </div>

      <div className={styles.seatsList}>
        {selectedSeats.map((selectedSeat) => (
          <div key={selectedSeat.seat.id} className={styles.seatItem}>
            <div className={styles.seatInfo}>
              <span className={styles.seatNumber}>
                Row {selectedSeat.seat.row}, Seat {selectedSeat.seat.seatNumber}
              </span>
              <TicketTypeSelect
                seatId={selectedSeat.seat.id}
                ticketPrices={(selectedSeat.seat.ticketPrices || []).map(
                  (tp) => ({
                    ticketTypeId: tp.ticketTypeId,
                    ticketTypeName:
                      tp.ticketTypeName || `Type ${tp.ticketTypeId}`,
                    finalPrice: tp.finalPrice,
                    minAge: tp.minAge,
                    maxAge: tp.maxAge,
                    requiresDocument: tp.requiresDocument,
                    documentType: tp.documentType,
                  }),
                )}
                selectedTicketTypeId={selectedSeat.ticketTypeId}
                onSelect={onTicketTypeChange}
              />
            </div>
            <div className={styles.seatPrice}>
              {selectedSeat.price.toFixed(2)}₴
            </div>
          </div>
        ))}
      </div>

      <div className={styles.bonusSection}>
        <div className={styles.bonusHeader}>
          <h4>Use Bonus Points</h4>
          <Tooltip content={bonusRules} position="left">
            <button className={styles.infoButton}>ℹ️</button>
          </Tooltip>
        </div>
        <div className={styles.bonusInfo}>
          <span>
            Available: {bonusBalance} points ({bonusBalance.toFixed(2)}₴)
          </span>
          {loading && <span>Loading...</span>}
        </div>

        {bonusBalance > 0 && maxAvailablePoints > 0 && (
          <div className={styles.bonusControls}>
            <div className={styles.pointsInput}>
              <input
                type="number"
                min={0}
                max={maxAvailablePoints}
                value={bonusPointsToUse}
                onChange={(e) =>
                  handleBonusPointsChange(parseInt(e.target.value) || 0)
                }
                disabled={isBooking || loading}
              />
              <button
                className={styles.useAllButton}
                onClick={handleUseAllPoints}
                disabled={isBooking || loading}
              >
                Use Max
              </button>
            </div>
            <div className={styles.pointsLimits}>
              <span>Min: {minUsablePoints}</span>
              <span>Max: {maxAvailablePoints}</span>
            </div>
          </div>
        )}
      </div>

      <div className={styles.summary}>
        <div className={styles.priceBreakdown}>
          {bonusPointsToUse > 0 && (
            <>
              <div className={styles.priceRow}>
                <span>Total price:</span>
                <span>{totalPrice.toFixed(2)}₴</span>
              </div>
              <div className={styles.priceRow}>
                <span>Bonus discount:</span>
                <span className={styles.discount}>-{discount.toFixed(2)}₴</span>
              </div>
            </>
          )}
          <div className={styles.finalPriceRow}>
            <span>Amount to pay:</span>
            <span className={styles.finalPrice}>{finalPrice.toFixed(2)}₴</span>
          </div>
        </div>

        <Tooltip
          content="After booking, you will have 20 minutes to complete the payment"
          position="top"
        >
          <button
            className={styles.bookButton}
            onClick={handleBooking}
            disabled={isBooking || loading}
          >
            {isBooking
              ? "Processing..."
              : `Book Now - ${finalPrice.toFixed(2)}₴`}
          </button>
        </Tooltip>
      </div>
    </div>
  );
};
