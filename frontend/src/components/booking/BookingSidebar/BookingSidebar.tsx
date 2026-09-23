import React, { useState, useEffect } from "react";
import { Info } from "lucide-react";
import { Tooltip } from "@/components/ui/Tooltip/Tooltip";
import { useBonus } from "@/hooks/features/bonus/useBonus";
import { TicketTypeSelect } from "../TicketTypeSelect/TicketTypeSelect";
import { formatPrice } from "@/utils/formatters";
import { Button } from "@/components/ui/Button/Button";
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
  const [bonusPointsInput, setBonusPointsInput] = useState("");
  const { balance, getMyBalance, loading } = useBonus();

  useEffect(() => {
    getMyBalance({ showErrorNotification: false }).catch(() => {});
  }, [getMyBalance]);

  const bonusBalance = balance?.pointsBalance || 0;
  const minUsablePoints = balance?.minUsablePoints || 0;
  const maxUsablePoints = balance?.maxUsablePoints ?? Number.POSITIVE_INFINITY;
  const pointValue = balance ? Number(balance.pointValue) : 1;
  const maxDiscountPercentage = balance ? Number(balance.maxDiscountPercentage) : 0.5;

  const maxAvailablePoints = Math.min(
    bonusBalance,
    maxUsablePoints,
    Math.floor((totalPrice * maxDiscountPercentage) / pointValue),
  );

  const bonusPointsToUse =
    bonusPointsInput === ""
      ? 0
      : Math.max(0, Math.min(parseInt(bonusPointsInput, 10) || 0, maxAvailablePoints));

  const handleBonusPointsInputChange = (rawValue: string) => {
    if (rawValue === "") {
      setBonusPointsInput("");
      return;
    }

    const parsed = parseInt(rawValue, 10);
    if (Number.isNaN(parsed)) {
      return;
    }

    setBonusPointsInput(String(Math.max(0, Math.min(parsed, maxAvailablePoints))));
  };

  const handleUseAllPoints = () => {
    setBonusPointsInput(String(maxAvailablePoints));
  };

  const handleBooking = () => {
    onBooking(bonusPointsToUse);
  };

  const discount = bonusPointsToUse * pointValue;
  const finalPrice = totalPrice - discount;

  if (!selectedSeats.length) {
    return (
      <div className={styles.sidebar}>
        <div className={styles.empty}>
          <h3>No seats selected</h3>
          <p>Click on available seats to select them</p>
        </div>
      </div>
    );
  }

  const bonusRules = [
    `• 1 bonus point = ${formatPrice(pointValue)} discount`,
    `• Minimum points to use: ${minUsablePoints}`,
    `• Cannot cover more than ${maxDiscountPercentage * 100}% of total price`,
    `• Maximum usable: ${maxAvailablePoints} points (${formatPrice(maxAvailablePoints * pointValue)})`,
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
              {formatPrice(selectedSeat.price)}
            </div>
          </div>
        ))}
      </div>

      <div className={styles.bonusSection}>
        <div className={styles.bonusHeader}>
          <h4>Use Bonus Points</h4>
          <Tooltip content={bonusRules} position="left">
            <button className={styles.infoButton} aria-label="Bonus points information">
              <Info size={18} />
            </button>
          </Tooltip>
        </div>
        <div className={styles.bonusInfo}>
          <span>
            Available: {bonusBalance} points ({formatPrice(bonusBalance)})
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
                value={bonusPointsInput}
                placeholder="0"
                onChange={(e) => handleBonusPointsInputChange(e.target.value)}
                disabled={isBooking || loading}
                aria-label="Bonus points to use"
              />
              <Button
                variant="secondary"
                size="small"
                onClick={handleUseAllPoints}
                disabled={isBooking || loading}
              >
                Use Max
              </Button>
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
                <span>{formatPrice(totalPrice)}</span>
              </div>
              <div className={styles.priceRow}>
                <span>Bonus discount:</span>
                <span className={styles.discount}>-{formatPrice(discount)}</span>
              </div>
            </>
          )}
          <div className={styles.finalPriceRow}>
            <span>Amount to pay:</span>
            <span className={styles.finalPrice}>{formatPrice(finalPrice)}</span>
          </div>
        </div>

        <Tooltip
          content="After booking, you will have 20 minutes to complete the payment"
          position="top"
        >
          <Button
            variant="primary"
            size="large"
            fullWidth
            onClick={handleBooking}
            loading={isBooking}
            disabled={isBooking || loading}
          >
            {isBooking
              ? "Processing..."
              : `Book Now for ${formatPrice(finalPrice)}`}
          </Button>
        </Tooltip>
      </div>
    </div>
  );
};
