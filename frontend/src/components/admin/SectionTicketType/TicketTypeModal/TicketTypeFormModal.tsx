import React, { useState, useEffect } from 'react';
import { Modal } from '@/components/ui/Modal/Modal';
import { Button } from '@/components/ui/Button/Button';
import { Input } from '@/components/ui/Input/Input';
import { Select } from '@/components/ui/Select/Select';
import { useTicketType } from '@/hooks/features/ticketType/useTicketType';
import { useNotification } from '@/hooks/common/useNotification';
import type { TicketTypeAdminResponse, TicketTypeCreateRequest, TicketTypeUpdateRequest, TicketTypeCategory } from '@/types/ticketType';
import { TicketTypeCategoryDisplay } from '@/types/ticketType';
import styles from './TicketTypeModal.module.css';

interface TicketTypeFormModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: (ticketType?: TicketTypeAdminResponse) => void;
    ticketType?: TicketTypeAdminResponse | null;
}

const TicketTypeFormModal: React.FC<TicketTypeFormModalProps> = ({
    isOpen,
    onClose,
    onSuccess,
    ticketType
}) => {
    const { create, update, loading } = useTicketType();
    const { showNotification } = useNotification();
    const isEditing = !!ticketType;

    const [formData, setFormData] = useState<TicketTypeCreateRequest | TicketTypeUpdateRequest>({
        displayName: '',
        category: 'STANDARD',
        priceMultiplier: '1.0',
        minAge: undefined,
        maxAge: undefined,
        requiresDocument: false,
        documentType: undefined,
        active: true
    });
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
        setError(null);
    }, [ticketType, isOpen]);

    const categoryOptions = Object.entries(TicketTypeCategoryDisplay).map(([value, label]) => ({
        value,
        label
    }));

    const handleInputChange = (field: keyof (TicketTypeCreateRequest | TicketTypeUpdateRequest), value: any) => {
        setFormData(prev => ({ ...prev, [field]: value }));
        if (error) setError(null);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        try {
            let result;

            if (isEditing && ticketType) {
                const updateData: TicketTypeUpdateRequest = {};

                if (formData.displayName !== ticketType.displayName) {
                    updateData.displayName = formData.displayName;
                }
                if (formData.category !== ticketType.category) {
                    updateData.category = formData.category as TicketTypeCategory;
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
                    showNotification('No changes to save', 'info');
                    onClose();
                    return;
                }

                result = await update(ticketType.id, updateData, ticketType.displayName);
                onSuccess(result || undefined);
                onClose();
            } else {
                result = await create(formData as TicketTypeCreateRequest);
                onSuccess(result || undefined);
                onClose();
            }
        } catch (err: any) {
            const errorMessage = err.message || `Failed to ${isEditing ? 'update' : 'create'} ticket type`;
            showNotification(errorMessage, 'error');
            setError(errorMessage);
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
                            value={formData.category || 'STANDARD'}
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
                            value={formData.priceMultiplier || '1.0'}
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
                        {isEditing ? 'Save Changes' : 'Create Ticket Type'}
                    </Button>
                </div>
            </form>
        </Modal>
    );
};

export default TicketTypeFormModal;