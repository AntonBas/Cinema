import React, { useState } from 'react';
import { Modal } from '@/components/ui/Modal';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { Select } from '@/components/ui/Select';
import type { CinemaHallRequest, SeatType } from '@/types';

interface CreateHallModalProps {
    onClose: () => void;
    onCreate: (request: CinemaHallRequest) => Promise<void>;
    loading?: boolean;
}

const seatTypeOptions = [
    { value: 'STANDARD', label: 'Standard' },
    { value: 'VIP', label: 'VIP' },
    { value: 'DISABLED', label: 'Disabled' },
    { value: 'COUPLE', label: 'Couple' }
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
        defaultSeatType: 'STANDARD' as SeatType
    });

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!formData.name.trim() || loading) return;

        try {
            await onCreate(formData);
        } catch (err) {
        }
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
                <div style={{ marginBottom: '1.5rem' }}>
                    <label htmlFor="hallName" style={{
                        display: 'block',
                        color: '#ffffff',
                        fontWeight: 600,
                        marginBottom: '0.5rem'
                    }}>
                        Hall Name *
                    </label>
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

                <div style={{
                    display: 'grid',
                    gridTemplateColumns: '1fr 1fr',
                    gap: '1rem',
                    marginBottom: '1.5rem'
                }}>
                    <div>
                        <label style={{
                            display: 'block',
                            color: '#ffffff',
                            fontWeight: 600,
                            marginBottom: '0.5rem'
                        }}>
                            Number of Rows *
                        </label>
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

                    <div>
                        <label style={{
                            display: 'block',
                            color: '#ffffff',
                            fontWeight: 600,
                            marginBottom: '0.5rem'
                        }}>
                            Seats Per Row *
                        </label>
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

                <div style={{ marginBottom: '1.5rem' }}>
                    <label style={{
                        display: 'block',
                        color: '#ffffff',
                        fontWeight: 600,
                        marginBottom: '0.5rem'
                    }}>
                        Default Seat Type
                    </label>
                    <Select
                        value={formData.defaultSeatType || 'STANDARD'}
                        onChange={(value) => updateField('defaultSeatType', value as SeatType)}
                        options={seatTypeOptions}
                        disabled={loading}
                    />
                </div>

                <div style={{
                    backgroundColor: '#2a2f3d',
                    padding: '1rem',
                    borderRadius: '8px',
                    marginBottom: '1.5rem',
                    border: '1px solid #3a4051'
                }}>
                    <h4 style={{
                        color: '#ffffff',
                        margin: '0 0 0.5rem 0',
                        fontSize: '0.9rem',
                        fontWeight: 600
                    }}>
                        Layout Summary
                    </h4>
                    <div style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(3, 1fr)',
                        gap: '0.5rem',
                        color: '#a0a4b8',
                        fontSize: '0.8rem'
                    }}>
                        <div>Rows: <strong>{formData.rows}</strong></div>
                        <div>Seats/Row: <strong>{formData.seatsPerRow}</strong></div>
                        <div>Total: <strong>{totalSeats}</strong></div>
                    </div>
                </div>

                <div style={{
                    display: 'flex',
                    gap: '1rem',
                    justifyContent: 'flex-end',
                    marginTop: '2rem',
                    paddingTop: '1.5rem',
                    borderTop: '1px solid #3a4051'
                }}>
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