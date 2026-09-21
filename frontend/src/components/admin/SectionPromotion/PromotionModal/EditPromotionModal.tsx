import React, { useState, useEffect } from 'react';
import { Modal, Button, Input } from '@/components/ui';
import { usePromotion } from '@/hooks/features/promotion/usePromotion';
import type { PromotionRequest, PromotionResponse } from '@/types/promotion';
import { toBackendFormat } from '@/utils/dateUtils';
import { isApiErrorException } from '@/utils/apiErrorHandler';
import styles from './PromotionModal.module.css';

interface EditPromotionModalProps {
    promotion: PromotionResponse;
    onClose: () => void;
    onSuccess: () => void;
}

const DESCRIPTION_LIMIT = 150;

const EditPromotionModal: React.FC<EditPromotionModalProps> = ({ promotion, onClose, onSuccess }) => {
    const { update, loading } = usePromotion();
    const [formData, setFormData] = useState({
        title: promotion.title,
        description: promotion.description || '',
        bonusPoints: promotion.bonusPoints.toString(),
        startDate: promotion.startDate?.split('T')[0] || '',
        endDate: promotion.endDate?.split('T')[0] || ''
    });
    const [dateError, setDateError] = useState('');
    const [errors, setErrors] = useState<Record<string, string>>({});

    useEffect(() => {
        setFormData({
            title: promotion.title,
            description: promotion.description || '',
            bonusPoints: promotion.bonusPoints.toString(),
            startDate: promotion.startDate?.split('T')[0] || '',
            endDate: promotion.endDate?.split('T')[0] || ''
        });
    }, [promotion]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (formData.startDate && formData.endDate) {
            const start = new Date(formData.startDate);
            const end = new Date(formData.endDate);
            if (end < start) {
                setDateError('End date must be after start date');
                return;
            }
        }
        setDateError('');

        const request: PromotionRequest = {
            title: formData.title,
            description: formData.description || undefined,
            bonusPoints: parseInt(formData.bonusPoints) || 100,
            startDate: formData.startDate ? toBackendFormat(formData.startDate) : undefined,
            endDate: formData.endDate ? toBackendFormat(formData.endDate) : undefined
        };

        try {
            const result = await update(promotion.id, request);
            if (result) {
                onSuccess();
                onClose();
            }
        } catch (err) {
            if (isApiErrorException(err) && err.isValidationError()) {
                setErrors(err.getValidationErrors());
            }
        }
    };

    const descriptionLength = formData.description.length;
    const isDescriptionValid = descriptionLength <= DESCRIPTION_LIMIT;

    return (
        <Modal isOpen={true} onClose={onClose} title="Edit Promotion" size="large">
            <form onSubmit={handleSubmit} className={styles.form}>
                <div className={styles.formGroup}>
                    <label className={styles.label}>Title *</label>
                    <Input
                        value={formData.title}
                        onChange={(value) => setFormData(prev => ({ ...prev, title: value }))}
                        required
                        error={errors.title}
                    />
                </div>

                <div className={styles.formGroup}>
                    <div className={styles.labelContainer}>
                        <label className={styles.label}>Description</label>
                        <span className={`${styles.counter} ${!isDescriptionValid ? styles.counterError : ''}`}>
                            {descriptionLength}/{DESCRIPTION_LIMIT}
                        </span>
                    </div>
                    <textarea
                        value={formData.description}
                        onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
                        rows={4}
                        className={`${styles.textarea} ${!isDescriptionValid ? styles.textareaError : ''}`}
                        maxLength={DESCRIPTION_LIMIT}
                    />
                    {errors.description && <div className={styles.error}>{errors.description}</div>}
                </div>

                <div className={styles.formGroup}>
                    <label className={styles.label}>Bonus Points *</label>
                    <Input
                        type="number"
                        value={formData.bonusPoints}
                        onChange={(value) => setFormData(prev => ({ ...prev, bonusPoints: value }))}
                        min="1"
                        required
                        error={errors.bonusPoints}
                    />
                </div>

                <div className={styles.dateRow}>
                    <div className={styles.formGroup}>
                        <label className={styles.label}>Start Date</label>
                        <Input
                            type="date"
                            value={formData.startDate}
                            onChange={(value) => setFormData(prev => ({ ...prev, startDate: value }))}
                            error={errors.startDate}
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.label}>End Date</label>
                        <Input
                            type="date"
                            value={formData.endDate}
                            onChange={(value) => setFormData(prev => ({ ...prev, endDate: value }))}
                            error={errors.endDate}
                        />
                    </div>
                </div>

                {dateError && <div className={styles.error}>{dateError}</div>}

                <div className={styles.actions}>
                    <Button type="button" variant="cancel" onClick={onClose}>
                        Cancel
                    </Button>
                    <Button type="submit" variant="primary" loading={loading}>
                        Update
                    </Button>
                </div>
            </form>
        </Modal>
    );
};

export default EditPromotionModal;