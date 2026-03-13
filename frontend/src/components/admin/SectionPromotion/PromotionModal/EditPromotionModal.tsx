import React, { useState, useEffect, useRef } from 'react';
import { Modal } from '@/components/ui/Modal/Modal';
import { Button } from '@/components/ui/Button/Button';
import { Input } from '@/components/ui/Input/Input';
import { usePromotion } from '@/hooks/features/promotion/usePromotion';
import { toBackendFormat } from '@/utils/dateUtils';
import styles from './PromotionModal.module.css';

interface EditPromotionModalProps {
    promotionId: number;
    onClose: () => void;
    onSuccess: (result: any) => void;
}

const DESCRIPTION_LIMIT = 500;

const EditPromotionModal: React.FC<EditPromotionModalProps> = ({
    promotionId,
    onClose,
    onSuccess
}) => {
    const { getById, update, loading } = usePromotion();
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        bonusPoints: '',
        startDate: '',
        endDate: ''
    });
    const [dateError, setDateError] = useState('');
    const fetchedRef = useRef(false);

    useEffect(() => {
        if (fetchedRef.current) return;
        fetchedRef.current = true;

        const fetchPromotion = async () => {
            try {
                const response = await getById(promotionId);
                if (response) {
                    const promotion = response;
                    setFormData({
                        title: promotion.title,
                        description: promotion.description || '',
                        bonusPoints: promotion.bonusPoints.toString(),
                        startDate: promotion.startDate ? promotion.startDate.split('T')[0] : '',
                        endDate: promotion.endDate ? promotion.endDate.split('T')[0] : ''
                    });
                }
            } catch (error) {
                onClose();
            }
        };
        fetchPromotion();
    }, [promotionId, getById, onClose]);

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

        try {
            const submissionData = {
                title: formData.title,
                description: formData.description || undefined,
                bonusPoints: parseInt(formData.bonusPoints) || 100,
                startDate: formData.startDate ? toBackendFormat(formData.startDate) : undefined,
                endDate: formData.endDate ? toBackendFormat(formData.endDate) : undefined
            };

            const response = await update(promotionId, submissionData);
            if (response) {
                onSuccess(response);
                onClose();
            }
        } catch (err) {
            throw err;
        }
    };

    const handleChange = (field: string, value: string) => {
        setFormData(prev => ({ ...prev, [field]: value }));
    };

    const handleClose = () => {
        onClose();
    };

    const descriptionLength = formData.description.length;
    const isDescriptionValid = descriptionLength <= DESCRIPTION_LIMIT;

    return (
        <Modal isOpen={true} onClose={handleClose} title="Edit Promotion" size="large">
            <form onSubmit={handleSubmit} className={styles.form}>
                <div className={styles.formGroup}>
                    <label className={styles.label}>Title *</label>
                    <Input
                        value={formData.title}
                        onChange={(value) => handleChange('title', value)}
                        required
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
                        onChange={(e) => handleChange('description', e.target.value)}
                        rows={4}
                        className={`${styles.textarea} ${!isDescriptionValid ? styles.textareaError : ''}`}
                        maxLength={DESCRIPTION_LIMIT}
                    />
                </div>

                <div className={styles.formGroup}>
                    <label className={styles.label}>Bonus Points *</label>
                    <Input
                        type="number"
                        value={formData.bonusPoints}
                        onChange={(value) => handleChange('bonusPoints', value)}
                        min="1"
                        required
                    />
                </div>

                <div className={styles.dateRow}>
                    <div className={styles.formGroup}>
                        <label className={styles.label}>Start Date</label>
                        <Input
                            type="date"
                            value={formData.startDate}
                            onChange={(value) => handleChange('startDate', value)}
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.label}>End Date</label>
                        <Input
                            type="date"
                            value={formData.endDate}
                            onChange={(value) => handleChange('endDate', value)}
                        />
                    </div>
                </div>

                {dateError && <div className={styles.error}>{dateError}</div>}

                <div className={styles.actions}>
                    <Button type="button" variant="cancel" onClick={handleClose}>
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