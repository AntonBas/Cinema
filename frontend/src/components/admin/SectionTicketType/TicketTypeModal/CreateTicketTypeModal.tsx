import React, { useState } from 'react';
import { Modal } from '@/components/ui/Modal/Modal';
import { Button } from '@/components/ui/Button/Button';
import { Input } from '@/components/ui/Input/Input';
import { Select } from '@/components/ui/Select/Select';
import { Notification } from '@/components/ui/Notification/Notification';
import { useTicketType } from '@/hooks/features/ticketType/useTicketType';
import type { TicketTypeCreateRequest, TicketTypeCategory } from '@/types/ticketType';
import { TicketTypeCategoryDisplay } from '@/types/ticketType';
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
    const { create, loading } = useTicketType();
    const [formData, setFormData] = useState<TicketTypeCreateRequest>({
        code: '',
        displayName: '',
        category: 'STANDARD',
        priceMultiplier: '1.0',
        minAge: undefined,
        maxAge: undefined,
        requiresDocument: false,
        documentType: undefined,
        active: true
    });
    const [showNotification, setShowNotification] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const categoryOptions = Object.entries(TicketTypeCategoryDisplay).map(([value, label]) => ({
        value,
        label
    }));

    const handleInputChange = (field: keyof TicketTypeCreateRequest, value: any) => {
        setFormData(prev => ({ ...prev, [field]: value }));
        if (error) setError(null);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        try {
            const response = await create(formData);

            if (response) {
                setShowNotification(true);
                setTimeout(() => {
                    setShowNotification(false);
                    onSuccess();
                    setFormData({
                        code: '',
                        displayName: '',
                        category: 'STANDARD',
                        priceMultiplier: '1.0',
                        minAge: undefined,
                        maxAge: undefined,
                        requiresDocument: false,
                        documentType: undefined,
                        active: true
                    });
                }, 1500);
            }
        } catch (err: any) {
            setError(err.message || 'Failed to create ticket type');
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
                                checked={formData.active}
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