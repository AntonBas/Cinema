import React, { useState, useEffect } from 'react';
import { Tooltip } from '@/components/ui/Tooltip/Tooltip';
import type { SelectedSeat } from '@/hooks/features/seatAvailability/useSeatSelection';
import { TicketTypeSelect } from '../TicketTypeSelect';
import styles from './BookingSidebar.module.css';

interface BookingSidebarProps {
    selectedSeats: SelectedSeat[];
    totalPrice: number;
    sessionId: number;
    onTicketTypeChange: (seatId: number, ticketTypeId: number) => void;
    onBooking: (bonusPointsToUse: number) => Promise<void>;
    isBooking: boolean;
    bonusBalance: number;
    maxUsablePoints: number;
    minUsablePoints: number;
}

export const BookingSidebar: React.FC<BookingSidebarProps> = ({
    selectedSeats,
    totalPrice,
    onTicketTypeChange,
    onBooking,
    isBooking,
    bonusBalance,
    maxUsablePoints,
    minUsablePoints
}) => {
    const [bonusPointsToUse, setBonusPointsToUse] = useState<number>(0);
    const [useAllPoints, setUseAllPoints] = useState(false);
    const [bonusError, setBonusError] = useState<string | null>(null);

    const calculateMaxAvailablePoints = () => {
        const maxPointsFromBalance = Math.min(bonusBalance, maxUsablePoints);
        const maxPointsFromTotalPrice = Math.floor(totalPrice * 0.50);
        return Math.min(maxPointsFromBalance, maxPointsFromTotalPrice);
    };

    useEffect(() => {
        if (useAllPoints) {
            const points = calculateMaxAvailablePoints();
            setBonusPointsToUse(points);
            validateBonusPoints(points);
        }
    }, [useAllPoints, bonusBalance, maxUsablePoints, totalPrice]);

    const validateBonusPoints = (points: number) => {
        setBonusError(null);

        if (points === 0) {
            return true;
        }

        if (points < minUsablePoints) {
            setBonusError(`Minimum ${minUsablePoints} points required`);
            return false;
        }

        if (points > maxUsablePoints) {
            setBonusError(`Maximum ${maxUsablePoints} points allowed per booking`);
            return false;
        }

        if (points > bonusBalance) {
            setBonusError(`You only have ${bonusBalance} points available`);
            return false;
        }

        const maxFromPrice = Math.floor(totalPrice * 0.50);
        if (points > maxFromPrice) {
            setBonusError(`Cannot use more than ${maxFromPrice} points (50% of total)`);
            return false;
        }

        return true;
    };

    const handleBonusPointsChange = (points: number) => {
        const validPoints = Math.max(0, Math.min(points, calculateMaxAvailablePoints()));
        setBonusPointsToUse(validPoints);
        setUseAllPoints(false);
        validateBonusPoints(validPoints);
    };

    const handleUseAllPoints = () => {
        const points = calculateMaxAvailablePoints();
        setUseAllPoints(true);
        validateBonusPoints(points);
    };

    const calculateDiscount = (points: number): number => {
        return points;
    };

    const calculateFinalPrice = (): number => {
        const discount = calculateDiscount(bonusPointsToUse);
        return Math.max(0, totalPrice - discount);
    };

    if (selectedSeats.length === 0) {
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

    const discount = calculateDiscount(bonusPointsToUse);
    const finalPrice = calculateFinalPrice();
    const maxAvailablePoints = calculateMaxAvailablePoints();
    const canUseAllPoints = bonusBalance > 0 && maxAvailablePoints > 0;

    const bonusRules = [
        `• 1 bonus point = 1 ₴ discount`,
        `• Minimum points to use: ${minUsablePoints}`,
        `• Cannot cover more than 50% of total price`,
        `• Maximum usable: ${maxAvailablePoints} points (₴${maxAvailablePoints.toFixed(2)})`,
        `• Points must be used in whole numbers`,
        `• Points expire according to bonus program terms`
    ].join('\n');

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
                            <div className={styles.ticketTypeContainer}>
                                <TicketTypeSelect
                                    seatId={selectedSeat.seat.id}
                                    ticketPrices={selectedSeat.seat.ticketPrices || []}
                                    selectedTicketTypeId={selectedSeat.ticketTypeId}
                                    onSelect={onTicketTypeChange}
                                />
                            </div>
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
                    <span className={styles.balanceText}>
                        Available: {bonusBalance} points (₴{bonusBalance.toFixed(2)})
                    </span>
                </div>

                {canUseAllPoints && (
                    <div className={styles.bonusControls}>
                        <div className={styles.pointsInput}>
                            <input
                                type="number"
                                min={0}
                                max={maxAvailablePoints}
                                value={bonusPointsToUse}
                                onChange={(e) => handleBonusPointsChange(parseInt(e.target.value) || 0)}
                                disabled={isBooking}
                                placeholder={`Min ${minUsablePoints} points`}
                            />
                            <button
                                className={styles.useAllButton}
                                onClick={handleUseAllPoints}
                                disabled={isBooking || !canUseAllPoints}
                            >
                                Use Max
                            </button>
                        </div>
                        {bonusError && (
                            <div className={styles.bonusError}>{bonusError}</div>
                        )}
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
                                <span>Bonus points:</span>
                                <span>{bonusPointsToUse} points</span>
                            </div>
                            <div className={styles.priceRow}>
                                <span>Bonus discount:</span>
                                <span className={styles.discount}>-₴{discount.toFixed(2)}</span>
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
                        onClick={() => {
                            if (validateBonusPoints(bonusPointsToUse)) {
                                onBooking(bonusPointsToUse);
                            }
                        }}
                        disabled={isBooking || selectedSeats.length === 0 || !!bonusError}
                    >
                        {isBooking ? 'Processing...' : `Book Now - ₴${finalPrice.toFixed(2)}`}
                    </button>
                </Tooltip>
            </div>
        </div>
    );
};