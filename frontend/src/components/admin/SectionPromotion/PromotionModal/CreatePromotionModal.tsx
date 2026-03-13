import React, { useState } from 'react';
import { Modal, Button, Input } from '@/components/ui';
import { usePromotion } from '@/hooks/features/promotion/usePromotion';
import { toBackendFormat } from '@/utils/dateUtils';
import styles from './PromotionModal.module.css';

interface CreatePromotionModalProps {
    onClose: () => void;
    onSuccess: (result: any) => void;
}

const DESCRIPTION_LIMIT = 500;

const CreatePromotionModal: React.FC<CreatePromotionModalProps> = ({
    onClose,
    onSuccess
}) => {
    const { create, loading } = usePromotion();
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        bonusPoints: '100',
        startDate: '',
        endDate: ''
    });
    const [dateError, setDateError] = useState('');

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

            const result = await create(submissionData);
            if (result) {
                onSuccess(result);
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
        <Modal isOpen={true} onClose={handleClose} title="Create Promotion" size="large">
            <form onSubmit={handleSubmit} className={styles.form}>
                <div className={styles.formGroup}>
                    <label className={styles.label}>Title *</label>
                    <Input
                        value={formData.title}
                        onChange={(value) => handleChange('title', value)}
                        placeholder="Enter title"
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
                        placeholder="Enter description"
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
                        placeholder="Enter bonus points"
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
                        Create
                    </Button>
                </div>
            </form>
        </Modal>
    );
};

export default CreatePromotionModal;