import React, { useState, useCallback } from 'react';
import type { CinemaHallRequest } from '@/types/cinemaHall';
import { SeatType } from '@/types/seat';
import { BaseHallModal } from './BaseHallModal';

interface CreateHallModalProps {
    onClose: () => void;
    onCreate: (request: CinemaHallRequest) => Promise<void>;
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
        defaultSeatType: SeatType.STANDARD,
        coupleRows: []
    });

    const handleSubmit = useCallback(async (e: React.FormEvent) => {
        e.preventDefault();
        if (!formData.name || loading) return;
        await onCreate(formData);
    }, [formData, loading, onCreate]);

    const updateField = useCallback(<K extends keyof CinemaHallRequest>(
        field: K,
        value: CinemaHallRequest[K]
    ) => {
        setFormData(prev => ({ ...prev, [field]: value }));
        if (field === 'rows') {
            setFormData(prev => ({
                ...prev,
                coupleRows: (prev.coupleRows || []).filter(row => row <= (value as number))
            }));
        }
    }, []);

    const handleCoupleRowsChange = useCallback((rows: number[]) => {
        setFormData(prev => ({ ...prev, coupleRows: rows }));
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
            coupleRows={formData.coupleRows || []}
            onCoupleRowsChange={handleCoupleRowsChange}
        />
    );
};