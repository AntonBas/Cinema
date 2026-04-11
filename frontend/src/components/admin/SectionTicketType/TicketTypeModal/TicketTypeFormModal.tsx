import React, { useState, useEffect } from 'react';
import { Modal } from '@/components/ui/Modal/Modal';
import { Button } from '@/components/ui/Button/Button';
import { Input } from '@/components/ui/Input/Input';
import { Select } from '@/components/ui/Select/Select';
import { useTicketType } from '@/hooks/features/ticketType/useTicketType';
import type { TicketTypeResponse, TicketTypeRequest, TicketTypeCategory } from '@/types/ticketType';
import { TicketTypeCategoryDisplay } from '@/types/ticketType';
import styles from './TicketTypeModal.module.css';

interface TicketTypeFormModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
    ticketType?: TicketTypeResponse | null;
}

const TicketTypeFormModal: React.FC<TicketTypeFormModalProps> = ({
    isOpen,
    onClose,
    onSuccess,
    ticketType
}) => {
    const { create, update, loading } = useTicketType();
    const isEditing = !!ticketType;

    const [formData, setFormData] = useState<TicketTypeRequest>({
        displayName: '',
        category: 'STANDARD',
        priceMultiplier: '1.0',
        minAge: undefined,
        maxAge: undefined,
        requiresDocument: false,
        documentType: undefined,
        active: true
    });

    useEffect(() => {
        if (ticketType) {
            setFormData({
                displayName: ticketType.displayName,
                category: ticketType.category,
                priceMultiplier: ticketType.priceMultiplier,
                minAge: ticketType.minAge,
                maxAge: ticketType.maxAge,
                requiresDocument: ticketType.requiresDocument,
                documentType: ticketType.documentType,
                active: ticketType.active
            });
        } else {
            setFormData({
                displayName: '',
                category: 'STANDARD',
                priceMultiplier: '1.0',
                minAge: undefined,
                maxAge: undefined,
                requiresDocument: false,
                documentType: undefined,
                active: true
            });
        }
    }, [ticketType, isOpen]);

    const categoryOptions = Object.entries(TicketTypeCategoryDisplay).map(([value, label]) => ({
        value,
        label
    }));

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        const result = isEditing && ticketType
            ? await update(ticketType.id, formData)
            : await create(formData);

        if (result) {
            onSuccess();
            onClose();
        }
    };

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title={isEditing ? `Edit Ticket Type: ${ticketType?.displayName}` : 'Create New Ticket Type'}
            size="large"
        >
            <form onSubmit={handleSubmit} className={styles.form}>
                <div className={`${styles.formRow} ${styles.formRowFull}`}>
                    <div className={styles.formGroup}>
                        <label className={`${styles.label} ${styles.required}`}>Display Name</label>
                        <Input
                            type="text"
                            value={formData.displayName}
                            onChange={(value) => setFormData(prev => ({ ...prev, displayName: value }))}
                            placeholder="e.g., Adult, Child"
                            required
                        />
                    </div>
                </div>

                <div className={styles.formRow}>
                    <div className={styles.formGroup}>
                        <label className={`${styles.label} ${styles.required}`}>Category</label>
                        <Select
                            options={categoryOptions}
                            value={formData.category}
                            onChange={(value) => setFormData(prev => ({ ...prev, category: value as TicketTypeCategory }))}
                            placeholder="Select category"
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label className={`${styles.label} ${styles.required}`}>Price Multiplier</label>
                        <Input
                            type="number"
                            value={formData.priceMultiplier}
                            onChange={(value) => setFormData(prev => ({ ...prev, priceMultiplier: value }))}
                            placeholder="e.g., 1.0"
                            step="0.01"
                            min="0"
                            required
                        />
                    </div>
                </div>

                <div className={styles.formRow}>
                    <div className={styles.formGroup}>
                        <label className={styles.label}>Min Age</label>
                        <Input
                            type="number"
                            value={formData.minAge?.toString() || ''}
                            onChange={(value) => setFormData(prev => ({ ...prev, minAge: value ? parseInt(value) : undefined }))}
                            placeholder="Optional"
                            min="0"
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.label}>Max Age</label>
                        <Input
                            type="number"
                            value={formData.maxAge?.toString() || ''}
                            onChange={(value) => setFormData(prev => ({ ...prev, maxAge: value ? parseInt(value) : undefined }))}
                            placeholder="Optional"
                            min="0"
                        />
                    </div>
                </div>

                <div className={styles.checkboxGroup}>
                    <input
                        type="checkbox"
                        id="requiresDocument"
                        checked={formData.requiresDocument}
                        onChange={(e) => setFormData(prev => ({ ...prev, requiresDocument: e.target.checked }))}
                    />
                    <label htmlFor="requiresDocument" className={styles.checkboxLabel}>
                        Requires Document Verification
                    </label>
                </div>

                {formData.requiresDocument && (
                    <div className={styles.formGroup}>
                        <label className={styles.label}>Document Type</label>
                        <Input
                            type="text"
                            value={formData.documentType || ''}
                            onChange={(value) => setFormData(prev => ({ ...prev, documentType: value || undefined }))}
                            placeholder="e.g., Student ID, Military ID"
                        />
                    </div>
                )}

                <div className={styles.checkboxGroup}>
                    <input
                        type="checkbox"
                        id="active"
                        checked={formData.active}
                        onChange={(e) => setFormData(prev => ({ ...prev, active: e.target.checked }))}
                    />
                    <label htmlFor="active" className={styles.checkboxLabel}>
                        Active (available for purchase)
                    </label>
                </div>

                <div className={styles.actions}>
                    <Button variant="cancel" onClick={onClose} disabled={loading}>
                        Cancel
                    </Button>
                    <Button type="submit" variant="primary" loading={loading} disabled={loading}>
                        {isEditing ? 'Save Changes' : 'Create Ticket Type'}
                    </Button>
                </div>
            </form>
        </Modal>
    );
};

export default TicketTypeFormModal;