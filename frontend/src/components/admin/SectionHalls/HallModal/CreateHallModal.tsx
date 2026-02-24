import React, { useState, useCallback } from 'react';
import type { CinemaHallRequest } from '@/types/cinemaHall';
import { SeatType } from '@/types/seat';
import { BaseHallModal } from './BaseHallModal';

interface CreateHallModalProps {
    onClose: () => void;
    onCreate: (request: CinemaHallRequest & { coupleRows?: number[] }) => Promise<void>;
    loading?: boolean;
}

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
    const [coupleRows, setCoupleRows] = useState<number[]>([]);

    const handleSubmit = useCallback(async (e: React.FormEvent) => {
        e.preventDefault();
        if (!formData.name.trim() || loading) return;
        await onCreate({ ...formData, coupleRows });
    }, [formData, coupleRows, loading, onCreate]);

    const updateField = useCallback(<K extends keyof CinemaHallRequest>(
        field: K,
        value: CinemaHallRequest[K]
    ) => {
        setFormData(prev => ({ ...prev, [field]: value }));
        if (field === 'rows') {
            setCoupleRows(prev => prev.filter(row => row <= (value as number)));
        }
    }, []);

    return (
        <BaseHallModal
            isOpen={true}
            title="Create New Hall"
            formData={formData}
            onClose={onClose}
            onSubmit={handleSubmit}
            onFieldChange={updateField}
            submitButtonText="Create Hall"
            loading={loading}
            showDefaultSeatType={true}
            coupleRows={coupleRows}
            onCoupleRowsChange={setCoupleRows}
        />
    );
};