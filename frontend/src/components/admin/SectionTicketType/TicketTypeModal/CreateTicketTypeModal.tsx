import React, { useState } from 'react';
import { Modal } from '@/components/ui/Modal';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Select } from '@/components/ui/Select';
import { Notification } from '@/components/ui/Notification';
import { useTicketTypeForm } from '@/hooks/features/ticketType/useTicketTypeForm';
import type { TicketTypeCreateRequest, TicketTypeCategory } from '@/types/ticketType';
import styles from './TicketTypeModal.module.css';

interface CreateTicketTypeModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
}

const CreateTicketTypeModal: React.FC<CreateTicketTypeModalProps> = ({
    isOpen,
    onClose,
    onSuccess
}) => {
    const { handleCreate, loading, error, getDefaultValues, getCategoryOptions } = useTicketTypeForm();
    const [formData, setFormData] = useState<TicketTypeCreateRequest>(getDefaultValues());
    const [showNotification, setShowNotification] = useState(false);

    const categoryOptions = getCategoryOptions().map(cat => ({
        value: cat.value,
        label: cat.label
    }));

    const handleInputChange = (field: keyof TicketTypeCreateRequest, value: any) => {
        setFormData(prev => ({ ...prev, [field]: value }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        const success = await handleCreate({
            ...formData,
            priceMultiplier: formData.priceMultiplier || '1.0',
            requiresDocument: formData.requiresDocument || false,
            active: formData.active !== undefined ? formData.active : true
        });

        if (success) {
            setShowNotification(true);
            setTimeout(() => {
                setShowNotification(false);
                onSuccess();
                setFormData(getDefaultValues());
            }, 1500);
        }
    };

    return (
        <>
            <Modal
                isOpen={isOpen}
                onClose={onClose}
                title="Create New Ticket Type"
                size="medium"
            >
                <form onSubmit={handleSubmit} className={styles.form}>
                    <div className={styles.formRow}>
                        <div className={styles.formGroup}>
                            <label className={`${styles.label} ${styles.required}`}>
                                Code
                            </label>
                            <Input
                                type="text"
                                value={formData.code}
                                onChange={(value) => handleInputChange('code', value)}
                                placeholder="e.g., ADULT, CHILD"
                                required
                            />
                        </div>

                        <div className={styles.formGroup}>
                            <label className={`${styles.label} ${styles.required}`}>
                                Display Name
                            </label>
                            <Input
                                type="text"
                                value={formData.displayName}
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
                                value={formData.category}
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
                                value={formData.priceMultiplier}
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
                                checked={formData.active !== undefined ? formData.active : true}
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
                            Create Ticket Type
                        </Button>
                    </div>
                </form>
            </Modal>

            {showNotification && (
                <Notification
                    id="create-success"
                    message="Ticket type created successfully!"
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

export default CreateTicketTypeModal;