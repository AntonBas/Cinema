import React, { useState, useEffect } from 'react';
import { Modal } from '@/components/ui/Modal';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Select } from '@/components/ui/Select';
import { Notification } from '@/components/ui/Notification';
import { useTicketType } from '@/hooks/features/ticketType/useTicketType';
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
    const { update, getCategoryOptions, loading: apiLoading } = useTicketType();
    const [formData, setFormData] = useState<TicketTypeUpdateRequest>({
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
    const [showNotification, setShowNotification] = useState(false);
    const [error, setError] = useState<string | null>(null);

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
        if (error) setError(null);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        try {
            const updateData: TicketTypeUpdateRequest = {
                code: formData.code,
                displayName: formData.displayName !== ticketType.displayName ? formData.displayName : undefined,
                category: formData.category !== ticketType.category ? formData.category : undefined,
                priceMultiplier: formData.priceMultiplier !== ticketType.priceMultiplier ? formData.priceMultiplier : undefined,
                minAge: formData.minAge !== ticketType.minAge ? formData.minAge : undefined,
                maxAge: formData.maxAge !== ticketType.maxAge ? formData.maxAge : undefined,
                requiresDocument: formData.requiresDocument !== ticketType.requiresDocument ? formData.requiresDocument : undefined,
                documentType: formData.documentType !== ticketType.documentType ? formData.documentType : undefined,
                active: formData.active !== ticketType.active ? formData.active : undefined,
            };

            // Видаляємо undefined значення
            const filteredData = Object.fromEntries(
                Object.entries(updateData).filter(([_, value]) => value !== undefined)
            );

            if (Object.keys(filteredData).length === 0) {
                onClose();
                return;
            }

            await update(ticketType.id, filteredData as TicketTypeUpdateRequest);

            setShowNotification(true);
            setTimeout(() => {
                setShowNotification(false);
                onSuccess();
            }, 1500);
        } catch (err: any) {
            setError(err.message || 'Failed to update ticket type');
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
                            <small className={styles.hint}>
                                Code cannot be changed
                            </small>
                        </div>

                        <div className={styles.formGroup}>
                            <label className={`${styles.label} ${styles.required}`}>
                                Display Name
                            </label>
                            <Input
                                type="text"
                                value={formData.displayName || ''}
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
                                checked={formData.requiresDocument || false}
                                onChange={(e) => handleInputChange('requiresDocument', e.target.checked)}
                            />
                            <label htmlFor="requiresDocument" className={styles.checkboxLabel}>
                                Requires Document Verification
                            </label>
                        </div>
                    </div>

                    {formData.requiresDocument && (
                        <div className={styles.formGroup}>
                            <label className={styles.label}>
                                Document Type
                            </label>
                            <Input
                                type="text"
                                value={formData.documentType || ''}
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
                                checked={formData.active || false}
                                onChange={(e) => handleInputChange('active', e.target.checked)}
                            />
                            <label htmlFor="active" className={styles.checkboxLabel}>
                                Active (available for purchase)
                            </label>
                        </div>
                    </div>

                    {error && (
                        <div className={styles.error}>
                            {error}
                        </div>
                    )}

                    <div className={styles.actions}>
                        <Button
                            variant="cancel"
                            onClick={onClose}
                            disabled={apiLoading}
                        >
                            Cancel
                        </Button>
                        <Button
                            type="submit"
                            variant="primary"
                            loading={apiLoading}
                            disabled={apiLoading}
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