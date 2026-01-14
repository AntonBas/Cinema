import React, { useState } from 'react';
import { Modal } from '@/components/ui/Modal';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { Select } from '@/components/ui/Select';
import type { CinemaHallRequest } from '@/types/cinemaHall';
import { SeatType } from '@/types/seat';
import styles from './CreateHallModal.module.css';

interface CreateHallModalProps {
    onClose: () => void;
    onCreate: (request: CinemaHallRequest) => Promise<void>;
    loading?: boolean;
}

const seatTypeOptions = [
    { value: SeatType.STANDARD, label: 'Standard' },
    { value: SeatType.VIP, label: 'VIP' },
    { value: SeatType.DISABLED, label: 'Disabled' },
    { value: SeatType.COUPLE, label: 'Couple' }
];

export const CreateHallModal: React.FC<CreateHallModalProps> = ({
    onClose,
    onCreate,
    loading = false
}) => {
    const [formData, setFormData] = useState<CinemaHallRequest>({
        name: '',
        rows: 10,
        seatsPerRow: 15,
        defaultSeatType: SeatType.STANDARD
    });

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!formData.name.trim() || loading) return;
        await onCreate(formData);
    };

    const updateField = <K extends keyof CinemaHallRequest>(
        field: K,
        value: CinemaHallRequest[K]
    ) => {
        setFormData(prev => ({ ...prev, [field]: value }));
    };

    const totalSeats = (formData.rows || 0) * (formData.seatsPerRow || 0);

    return (
        <Modal isOpen={true} onClose={onClose} title="Create New Hall" size="medium">
            <form onSubmit={handleSubmit}>
                <div className={styles.formGroup}>
                    <label className={styles.label}>Hall Name *</label>
                    <Input
                        type="text"
                        value={formData.name}
                        onChange={(value) => updateField('name', value)}
                        placeholder="Enter hall name"
                        required={true}
                        maxLength={25}
                        disabled={loading}
                    />
                </div>

                <div className={styles.rowContainer}>
                    <div className={styles.formGroup}>
                        <label className={styles.label}>Number of Rows *</label>
                        <Input
                            type="number"
                            value={formData.rows?.toString() || ''}
                            onChange={(value) => updateField('rows', parseInt(value) || 1)}
                            placeholder="Rows"
                            required={true}
                            min={1}
                            max={20}
                            disabled={loading}
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.label}>Seats Per Row *</label>
                        <Input
                            type="number"
                            value={formData.seatsPerRow?.toString() || ''}
                            onChange={(value) => updateField('seatsPerRow', parseInt(value) || 1)}
                            placeholder="Seats per row"
                            required={true}
                            min={1}
                            max={20}
                            disabled={loading}
                        />
                    </div>
                </div>

                <div className={styles.formGroup}>
                    <label className={styles.label}>Default Seat Type</label>
                    <Select
                        value={formData.defaultSeatType || SeatType.STANDARD}
                        onChange={(value) => updateField('defaultSeatType', value as SeatType)}
                        options={seatTypeOptions}
                        disabled={loading}
                    />
                </div>

                <div className={styles.summary}>
                    <h4 className={styles.summaryTitle}>Layout Summary</h4>
                    <div className={styles.summaryGrid}>
                        <div>Rows: <strong>{formData.rows}</strong></div>
                        <div>Seats/Row: <strong>{formData.seatsPerRow}</strong></div>
                        <div>Total: <strong>{totalSeats}</strong></div>
                    </div>
                </div>

                <div className={styles.actions}>
                    <Button
                        variant="cancel"
                        onClick={onClose}
                        disabled={loading}
                    >
                        Cancel
                    </Button>
                    <Button
                        type="submit"
                        variant="primary"
                        disabled={!formData.name.trim() || loading}
                        loading={loading}
                    >
                        Create Hall
                    </Button>
                </div>
            </form>
        </Modal>
    );
};