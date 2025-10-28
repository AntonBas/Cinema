import React, { useState, useEffect } from 'react';
import type { CinemaHallDto, HallLayoutDto, SeatLayoutRequest } from '@/types';
import { SeatType } from '@/types';
import { cinemaHallApi } from '@/api/cinemaHall';
import { useNotification } from '@/hooks/useNotification';
import styles from './HallLayoutModal.module.css';

interface HallLayoutModalProps {
    hall: CinemaHallDto;
    onClose: () => void;
    onSeatsGenerated?: () => void;
}

export const HallLayoutModal: React.FC<HallLayoutModalProps> = ({
    hall,
    onClose,
    onSeatsGenerated
}) => {
    const [layout, setLayout] = useState<HallLayoutDto | null>(null);
    const [loading, setLoading] = useState(true);
    const [showSeatForm, setShowSeatForm] = useState(false);
    const [generating, setGenerating] = useState(false);
    const [seatForm, setSeatForm] = useState<SeatLayoutRequest>({
        rows: 5,
        seatsPerRow: 10,
        defaultSeatType: SeatType.STANDARD
    });

    const { showNotification } = useNotification();

    const loadLayout = async () => {
        try {
            setLoading(true);
            const data = await cinemaHallApi.getHallLayout(hall.id!);
            setLayout(data);
        } catch (err: any) {
            if (err.message.includes('404') || err.message.includes('not found')) {
                setLayout(null);
            } else {
                showNotification(err.message || 'Failed to load hall layout', 'error');
            }
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadLayout();
    }, [hall.id]);

    const handleGenerateSeats = async () => {
        try {
            setGenerating(true);
            await cinemaHallApi.generateSeats(hall.id!, seatForm);
            await loadLayout();
            setShowSeatForm(false);
            onSeatsGenerated?.();
        } catch (err: any) {
            showNotification(err.message || 'Failed to generate seats', 'error');
        } finally {
            setGenerating(false);
        }
    };

    const renderSeats = () => {
        if (!layout || !layout.rows || layout.rows.length === 0) return null;

        return (
            <div className={styles.layout}>
                <div className={styles.screen}>Screen</div>
                <div className={styles.seats}>
                    {layout.rows.map(row => (
                        <div key={row.rowNumber} className={styles.row}>
                            <div className={styles.rowNumber}>Row {row.rowNumber}</div>
                            <div className={styles.seatsRow}>
                                {row.seats.map(seat => (
                                    <div
                                        key={seat.id}
                                        className={`${styles.seat} ${styles[seat.seatType.toLowerCase()]}`}
                                        title={`Row ${seat.row}, Seat ${seat.number} - ${seat.seatType}`}
                                    >
                                        {seat.number}
                                    </div>
                                ))}
                            </div>
                        </div>
                    ))}
                </div>
                <div className={styles.layoutInfo}>
                    <p>Total: {layout.totalSeats} seats in {layout.totalRows} rows</p>
                </div>
            </div>
        );
    };

    return (
        <div className={styles.overlay} onClick={onClose}>
            <div className={styles.modal} onClick={e => e.stopPropagation()}>
                <div className={styles.header}>
                    <h2>Hall Layout - {hall.name}</h2>
                    <button className={styles.closeButton} onClick={onClose}>×</button>
                </div>

                <div className={styles.layoutContent}>
                    {loading && <div className={styles.loading}>Loading layout...</div>}

                    {!loading && (!layout || layout.rows.length === 0) && (
                        <div className={styles.noLayout}>
                            <div className={styles.emptyIcon}>🎭</div>
                            <h3>No Seats Configured</h3>
                            <p>This hall doesn't have any seats yet.</p>
                            <button
                                className={styles.generateButton}
                                onClick={() => setShowSeatForm(true)}
                                disabled={generating}
                            >
                                {generating ? 'Generating...' : 'Generate Seats Layout'}
                            </button>
                        </div>
                    )}

                    {showSeatForm && (
                        <div className={styles.seatForm}>
                            <h3>Generate Seats Layout</h3>
                            <div className={styles.formRow}>
                                <label>
                                    Number of Rows:
                                    <input
                                        type="number"
                                        min="1"
                                        max="20"
                                        value={seatForm.rows}
                                        onChange={(e) => setSeatForm({ ...seatForm, rows: parseInt(e.target.value) || 1 })}
                                    />
                                </label>
                                <label>
                                    Seats per Row:
                                    <input
                                        type="number"
                                        min="1"
                                        max="20"
                                        value={seatForm.seatsPerRow}
                                        onChange={(e) => setSeatForm({ ...seatForm, seatsPerRow: parseInt(e.target.value) || 1 })}
                                    />
                                </label>
                                <label>
                                    Default Seat Type:
                                    <select
                                        value={seatForm.defaultSeatType}
                                        onChange={(e) => setSeatForm({ ...seatForm, defaultSeatType: e.target.value as SeatType })}
                                    >
                                        {Object.values(SeatType).map(type => (
                                            <option key={type} value={type}>
                                                {type.charAt(0) + type.slice(1).toLowerCase()}
                                            </option>
                                        ))}
                                    </select>
                                </label>
                            </div>
                            <div className={styles.formActions}>
                                <button
                                    className={styles.cancelButton}
                                    onClick={() => setShowSeatForm(false)}
                                    disabled={generating}
                                >
                                    Cancel
                                </button>
                                <button
                                    className={styles.generateButton}
                                    onClick={handleGenerateSeats}
                                    disabled={generating}
                                >
                                    {generating ? 'Generating...' : 'Generate Seats'}
                                </button>
                            </div>
                        </div>
                    )}

                    {renderSeats()}
                </div>
            </div>
        </div>
    );
};