import React, { useState, useEffect, useCallback, useMemo } from 'react';
import type { CinemaHallResponse, CinemaHallRequest } from '@/types/cinemaHall';
import { BaseHallModal } from './BaseHallModal';

interface EditHallModalProps {
    hall: CinemaHallResponse;
    onClose: () => void;
    onUpdate: (id: number, request: CinemaHallRequest) => Promise<void>;
    loading?: boolean;
}

export const EditHallModal: React.FC<EditHallModalProps> = ({
    hall,
    onClose,
    onUpdate,
    loading = false
}) => {
    const [formData, setFormData] = useState<CinemaHallRequest>({
        name: hall.name,
        rows: hall.rows,
        seatsPerRow: hall.seatsPerRow,
        defaultSeatType: hall.defaultSeatType,
        coupleRows: hall.coupleRows || []
    });

    useEffect(() => {
        setFormData({
            name: hall.name,
            rows: hall.rows,
            seatsPerRow: hall.seatsPerRow,
            defaultSeatType: hall.defaultSeatType,
            coupleRows: hall.coupleRows || []
        });
    }, [hall]);

    const handleSubmit = useCallback(async (e: React.FormEvent) => {
        e.preventDefault();
        if (!formData.name || loading) return;
        await onUpdate(hall.id, formData);
    }, [formData, hall.id, loading, onUpdate]);

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

    const hasChanges = useMemo(() =>
        formData.name !== hall.name ||
        formData.rows !== hall.rows ||
        formData.seatsPerRow !== hall.seatsPerRow ||
        formData.defaultSeatType !== hall.defaultSeatType ||
        JSON.stringify(formData.coupleRows) !== JSON.stringify(hall.coupleRows || []),
        [formData, hall]
    );

    return (
        <BaseHallModal
            isOpen={true}
            title="Edit Cinema Hall"
            formData={formData}
            onClose={onClose}
            onSubmit={handleSubmit}
            onFieldChange={updateField}
            submitButtonText="Update Hall"
            isSubmitDisabled={!hasChanges}
            loading={loading}
            showDefaultSeatType={true}
            coupleRows={formData.coupleRows || []}
            onCoupleRowsChange={handleCoupleRowsChange}
        />
    );
};