import React, { useState, useEffect } from 'react';
import { Modal } from '@/components/ui/Modal';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Select } from '@/components/ui/Select';
import { Notification } from '@/components/ui/Notification';
import { useTicketTypeForm } from '@/hooks/features/ticketType/useTicketTypeForm';
import type { TicketTypeResponse, TicketTypeUpdateRequest, TicketTypeCategory } from '@/types/ticketType';
import styles from './TicketTypeModal.module.css';

interface EditTicketTypeModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
    ticketType: TicketTypeResponse;
}

const EditTicketTypeModal: React.FC<EditTicketTypeModalProps> = ({
    isOpen,
    onClose,
    onSuccess,
    ticketType
}) => {
    const { handleUpdate, loading, error, getCategoryOptions } = useTicketTypeForm();
    const [formData, setFormData] = useState<TicketTypeUpdateRequest>({
        code: ''
    });
    const [showNotification, setShowNotification] = useState(false);

    useEffect(() => {
        if (ticketType) {
            setFormData({
                code: ticketType.code,
                displayName: ticketType.displayName,
                category: ticketType.category,
                priceMultiplier: ticketType.priceMultiplier,
                minAge: ticketType.minAge,
                maxAge: ticketType.maxAge,
                requiresDocument: ticketType.requiresDocument,
                documentType: ticketType.documentType,
                active: ticketType.active
            });
        }
    }, [ticketType]);

    const categoryOptions = getCategoryOptions().map(cat => ({
        value: cat.value,
        label: cat.label
    }));

    const handleInputChange = (field: keyof TicketTypeUpdateRequest, value: any) => {
        setFormData(prev => ({ ...prev, [field]: value }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        const success = await handleUpdate(ticketType.id, {
            ...formData,
            priceMultiplier: formData.priceMultiplier || ticketType.priceMultiplier
        });

        if (success) {
            setShowNotification(true);
            setTimeout(() => {
                setShowNotification(false);
                onSuccess();
            }, 1500);
        }
    };

    const DisabledInput = ({ value }: { value: string }) => (
        <div className={`${styles.disabledInput} ${styles.input}`}>
            <input
                type="text"
                value={value}
                disabled
                readOnly
                className={styles.input}
            />
        </div>
    );

    return (
        <>
            <Modal
                isOpen={isOpen}
                onClose={onClose}
                title={`Edit Ticket Type: ${ticketType.code}`}
                size="medium"
            >
                <form onSubmit={handleSubmit} className={styles.form}>
                    <div className={styles.formRow}>
                        <div className={styles.formGroup}>
                            <label className={styles.label}>
                                Code
                            </label>
                            <DisabledInput value={ticketType.code} />
                            <small style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                                Code cannot be changed
                            </small>
                        </div>

                        <div className={styles.formGroup}>
                            <label className={`${styles.label} ${styles.required}`}>
                                Display Name
                            </label>
                            <Input
                                type="text"
                                value={formData.displayName || ticketType.displayName}
                                onChange={(value) => handleInputChange('displayName', value)}
                                placeholder="e.g., Adult, Child"
                                required
                            />
                        </div>
                    </div>

                    <div className={styles.formRow}>
                        <div className={styles.formGroup}>
                            <label className={`${styles.label} ${styles.required}`}>
                                Category
                            </label>
                            <Select
                                options={categoryOptions}
                                value={formData.category || ticketType.category}
                                onChange={(value) => handleInputChange('category', value as TicketTypeCategory)}
                                placeholder="Select category"
                            />
                        </div>

                        <div className={styles.formGroup}>
                            <label className={`${styles.label} ${styles.required}`}>
                                Price Multiplier
                            </label>
                            <Input
                                type="number"
                                value={formData.priceMultiplier || ticketType.priceMultiplier}
                                onChange={(value) => handleInputChange('priceMultiplier', value)}
                                placeholder="e.g., 1.0"
                                step="0.01"
                                min="0"
                                required
                            />
                        </div>
                    </div>

                    <div className={styles.formRow}>
                        <div className={styles.formGroup}>
                            <label className={styles.label}>
                                Min Age
                            </label>
                            <Input
                                type="number"
                                value={formData.minAge?.toString() || ''}
                                onChange={(value) => handleInputChange('minAge', value ? parseInt(value) : null)}
                                placeholder="Optional"
                                min="0"
                            />
                        </div>

                        <div className={styles.formGroup}>
                            <label className={styles.label}>
                                Max Age
                            </label>
                            <Input
                                type="number"
                                value={formData.maxAge?.toString() || ''}
                                onChange={(value) => handleInputChange('maxAge', value ? parseInt(value) : null)}
                                placeholder="Optional"
                                min="0"
                            />
                        </div>
                    </div>

                    <div className={styles.formGroup}>
                        <div className={styles.checkboxGroup}>
                            <input
                                type="checkbox"
                                id="requiresDocument"
                                checked={formData.requiresDocument || ticketType.requiresDocument}
                                onChange={(e) => handleInputChange('requiresDocument', e.target.checked)}
                            />
                            <label htmlFor="requiresDocument" className={styles.checkboxLabel}>
                                Requires Document Verification
                            </label>
                        </div>
                    </div>

                    {(formData.requiresDocument || ticketType.requiresDocument) && (
                        <div className={styles.formGroup}>
                            <label className={styles.label}>
                                Document Type
                            </label>
                            <Input
                                type="text"
                                value={formData.documentType || ticketType.documentType || ''}
                                onChange={(value) => handleInputChange('documentType', value || null)}
                                placeholder="e.g., Student ID, Military ID"
                            />
                        </div>
                    )}

                    <div className={styles.formGroup}>
                        <div className={styles.checkboxGroup}>
                            <input
                                type="checkbox"
                                id="active"
                                checked={formData.active !== undefined ? formData.active : ticketType.active}
                                onChange={(e) => handleInputChange('active', e.target.checked)}
                            />
                            <label htmlFor="active" className={styles.checkboxLabel}>
                                Active (available for purchase)
                            </label>
                        </div>
                    </div>

                    {error && (
                        <div style={{ color: 'var(--error)', fontSize: '14px' }}>
                            {error}
                        </div>
                    )}

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
                            loading={loading}
                            disabled={loading}
                        >
                            Save Changes
                        </Button>
                    </div>
                </form>
            </Modal>

            {showNotification && (
                <Notification
                    id="edit-success"
                    message="Ticket type updated successfully!"
                    type="success"
                    isVisible={showNotification}
                    onClose={() => setShowNotification(false)}
                    duration={2000}
                    isStatic={false}
                />
            )}
        </>
    );
};

export default EditTicketTypeModal;