import React, { useState, useEffect } from 'react';
import { Modal, Button, Input } from '@/components/ui';
import { useAdminPromotion } from '@/hooks/features/promotion/useAdminPromotion';
import { usePromotionForm } from '@/hooks/features/promotion/usePromotionForm';
import { toBackendFormat } from '@/utils/dateUtils';
import styles from './PromotionModal.module.css';

interface EditPromotionModalProps {
    promotionId: number;
    onClose: () => void;
    onSuccess: () => void;
}

const EditPromotionModal: React.FC<EditPromotionModalProps> = ({
    promotionId,
    onClose,
    onSuccess
}) => {
    const { getById } = useAdminPromotion();
    const { handleUpdate, loading: updating, error, success, reset } = usePromotionForm();
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        bonusPoints: '',
        startDate: '',
        endDate: ''
    });
    const [dateError, setDateError] = useState('');

    useEffect(() => {
        const fetchPromotion = async () => {
            try {
                const promotion = await getById(promotionId);
                setFormData({
                    title: promotion.title,
                    description: promotion.description || '',
                    bonusPoints: promotion.bonusPoints.toString(),
                    startDate: promotion.startDate ? promotion.startDate.split('T')[0] : '',
                    endDate: promotion.endDate ? promotion.endDate.split('T')[0] : ''
                });
            } catch (error) {
            }
        };
        fetchPromotion();
    }, [promotionId, getById]);

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

        const submissionData = {
            title: formData.title,
            description: formData.description || undefined,
            bonusPoints: parseInt(formData.bonusPoints) || 100,
            startDate: formData.startDate ? toBackendFormat(formData.startDate) : undefined,
            endDate: formData.endDate ? toBackendFormat(formData.endDate) : undefined
        };

        const result = await handleUpdate(promotionId, submissionData);
        if (result) {
            setTimeout(() => {
                reset();
                onSuccess();
            }, 1000);
        }
    };

    const handleChange = (field: string, value: string) => {
        setFormData(prev => ({ ...prev, [field]: value }));
    };

    const handleClose = () => {
        reset();
        onClose();
    };

    return (
        <Modal isOpen={true} onClose={handleClose} title="Edit Promotion" size="medium">
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
                    <label className={styles.label}>Description</label>
                    <textarea
                        value={formData.description}
                        onChange={(e) => handleChange('description', e.target.value)}
                        rows={3}
                        className={styles.textarea}
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
                {error && <div className={styles.error}>{error}</div>}
                {success && <div className={styles.success}>Promotion updated successfully!</div>}

                <div className={styles.actions}>
                    <Button type="button" variant="cancel" onClick={handleClose}>
                        Cancel
                    </Button>
                    <Button type="submit" variant="primary" loading={updating}>
                        Update
                    </Button>
                </div>
            </form>
        </Modal>
    );
};

export default EditPromotionModal;