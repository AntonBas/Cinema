import React, { useState, useEffect } from 'react';
import { Modal } from '@/components/ui/Modal/Modal';
import { Button } from '@/components/ui/Button/Button';
import { Input } from '@/components/ui/Input/Input';
import { Select } from '@/components/ui/Select/Select';
import { Notification } from '@/components/ui/Notification/Notification';
import { useTicketType } from '@/hooks/features/ticketType/useTicketType';
import type { TicketTypeResponse, TicketTypeUpdateRequest, TicketTypeCategory } from '@/types/ticketType';
import { TicketTypeCategoryDisplay } from '@/types/ticketType';
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
    const { update, loading } = useTicketType();
    const [formData, setFormData] = useState<TicketTypeUpdateRequest>({
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

    const categoryOptions = Object.entries(TicketTypeCategoryDisplay).map(([value, label]) => ({
        value,
        label
    }));

    const handleInputChange = (field: keyof TicketTypeUpdateRequest, value: any) => {
        setFormData(prev => ({ ...prev, [field]: value }));
        if (error) setError(null);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        try {
            const updateData: TicketTypeUpdateRequest = {};

            if (formData.displayName !== ticketType.displayName) {
                updateData.displayName = formData.displayName;
            }
            if (formData.category !== ticketType.category) {
                updateData.category = formData.category;
            }
            if (formData.priceMultiplier !== ticketType.priceMultiplier) {
                updateData.priceMultiplier = formData.priceMultiplier;
            }
            if (formData.minAge !== ticketType.minAge) {
                updateData.minAge = formData.minAge;
            }
            if (formData.maxAge !== ticketType.maxAge) {
                updateData.maxAge = formData.maxAge;
            }
            if (formData.requiresDocument !== ticketType.requiresDocument) {
                updateData.requiresDocument = formData.requiresDocument;
            }
            if (formData.documentType !== ticketType.documentType) {
                updateData.documentType = formData.documentType;
            }
            if (formData.active !== ticketType.active) {
                updateData.active = formData.active;
            }

            if (Object.keys(updateData).length === 0) {
                onClose();
                return;
            }

            const response = await update(ticketType.id, updateData);

            if (response?.data) {
                setShowNotification(true);
                setTimeout(() => {
                    setShowNotification(false);
                    onSuccess();
                }, 1500);
            }
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
                                onChange={(value) => handleInputChange('minAge', value ? parseInt(value) : undefined)}
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
                                onChange={(value) => handleInputChange('maxAge', value ? parseInt(value) : undefined)}
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
                                onChange={(value) => handleInputChange('documentType', value || undefined)}
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