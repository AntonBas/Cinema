import React, { useState, useEffect } from 'react';
import { useUser } from '@/hooks/features/user/useUser';
import { Input, Button, Modal } from '@/components/ui';
import type { UserProfileResponse, UserUpdateRequest } from '@/types/user';
import styles from './ProfileEditForm.module.css';

interface ProfileEditFormProps {
    user: UserProfileResponse;
    onCancel: () => void;
    onSuccess: (data: UserUpdateRequest) => Promise<void>;
}

export const ProfileEditForm: React.FC<ProfileEditFormProps> = ({
    user,
    onCancel,
    onSuccess
}) => {
    const { isProfileUpdating } = useUser();
    const [formData, setFormData] = useState<UserUpdateRequest>({
        firstName: user.firstName,
        lastName: user.lastName,
        dateOfBirth: user.dateOfBirth,
        city: user.city || '',
        phoneNumber: user.phoneNumber || ''
    });
    const [successMessage, setSuccessMessage] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const [formErrors, setFormErrors] = useState<Record<string, string>>({});
    const [showDateChangeWarning, setShowDateChangeWarning] = useState(false);
    const [originalDateOfBirth] = useState(user.dateOfBirth);

    useEffect(() => {
        if (formData.dateOfBirth !== originalDateOfBirth && user.verificationStatus === 'VERIFIED') {
            setShowDateChangeWarning(true);
        }
    }, [formData.dateOfBirth, originalDateOfBirth, user.verificationStatus]);

    const handleChange = (field: string, value: string) => {
        setFormData(prev => ({
            ...prev,
            [field]: value
        }));

        if (formErrors[field]) {
            setFormErrors(prev => ({ ...prev, [field]: '' }));
        }
        if (errorMessage) setErrorMessage('');
        if (successMessage) setSuccessMessage('');
    };

    const validateForm = () => {
        const errors: Record<string, string> = {};

        if (!formData.firstName?.trim()) {
            errors.firstName = 'First name is required';
        }

        if (!formData.lastName?.trim()) {
            errors.lastName = 'Last name is required';
        }

        if (!formData.dateOfBirth) {
            errors.dateOfBirth = 'Date of birth is required';
        } else {
            const birthDate = new Date(formData.dateOfBirth);
            const today = new Date();
            if (birthDate > today) {
                errors.dateOfBirth = 'Date of birth cannot be in the future';
            }
        }

        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!validateForm()) {
            return;
        }

        try {
            await onSuccess(formData);
            setSuccessMessage('Profile updated successfully!');
        } catch (err) {
            setErrorMessage('Failed to update profile. Please try again.');
        }
    };

    const handleDateChangeContinue = () => {
        setShowDateChangeWarning(false);
    };

    const handleDateChangeCancel = () => {
        setFormData(prev => ({ ...prev, dateOfBirth: originalDateOfBirth }));
        setShowDateChangeWarning(false);
    };

    const isLoading = isProfileUpdating;

    return (
        <div className={styles.profileForm}>
            <Modal
                isOpen={showDateChangeWarning}
                onClose={handleDateChangeCancel}
                title="Date of Birth Change Warning"
                size="small"
            >
                <div className={styles.modalContent}>
                    <p className={styles.warningText}>
                        <strong>⚠️ Attention!</strong> You are about to change your verified date of birth.
                    </p>
                    <p className={styles.modalDescription}>
                        After changing your date of birth, you will need to verify it again at the cinema cash desk to access bonus for birthday.
                    </p>
                    <div className={styles.modalActions}>
                        <Button
                            variant="secondary"
                            onClick={handleDateChangeCancel}
                        >
                            Keep Original Date
                        </Button>
                        <Button
                            variant="primary"
                            onClick={handleDateChangeContinue}
                        >
                            Continue with Change
                        </Button>
                    </div>
                </div>
            </Modal>

            <form onSubmit={handleSubmit} className={styles.form}>
                {errorMessage && (
                    <div className={styles.notification} data-type="error">
                        {errorMessage}
                    </div>
                )}

                {successMessage && (
                    <div className={styles.notification} data-type="success">
                        {successMessage}
                    </div>
                )}

                {user.verificationStatus && (
                    <div
                        className={`${styles.verificationAlert} ${styles[user.verificationStatus.toLowerCase()]}`}
                    >
                        <div className={styles.alertIcon}>
                            {user.verificationStatus === 'VERIFIED' ? '✓' : '⚠️'}
                        </div>
                        <div className={styles.alertContent}>
                            <p className={styles.alertTitle}>
                                Date of Birth: {user.verificationStatus === 'VERIFIED' ? 'Verified' : 'Not Verified'}
                            </p>
                            <p className={styles.alertMessage}>
                                {user.verificationStatus === 'VERIFIED'
                                    ? 'Your date of birth is verified. If you change it, verification will be required again.'
                                    : 'Your date of birth needs verification at the cinema cash desk for age-restricted content access.'}
                            </p>
                            {user.verificationStatus === 'NOT_VERIFIED' && (
                                <p className={styles.alertNote}>
                                    To verify, visit any cinema cash desk with your ID document.
                                </p>
                            )}
                        </div>
                    </div>
                )}

                <div className={styles.formSection}>
                    <h2 className={styles.sectionTitle}>Personal Information</h2>

                    <div className={styles.formRow}>
                        <Input
                            type="text"
                            placeholder="Enter your first name"
                            value={formData.firstName || ''}
                            onChange={(value) => handleChange('firstName', value)}
                            disabled={isLoading}
                            error={formErrors.firstName}
                            label="First Name"
                        />
                        <Input
                            type="text"
                            placeholder="Enter your last name"
                            value={formData.lastName || ''}
                            onChange={(value) => handleChange('lastName', value)}
                            disabled={isLoading}
                            error={formErrors.lastName}
                            label="Last Name"
                        />
                    </div>

                    <div className={styles.dateOfBirthField}>
                        <Input
                            type="date"
                            value={formData.dateOfBirth || ''}
                            onChange={(value) => handleChange('dateOfBirth', value)}
                            disabled={isLoading}
                            error={formErrors.dateOfBirth}
                            label="Date of Birth"
                        />
                        {user.verificationStatus === 'VERIFIED' && formData.dateOfBirth !== originalDateOfBirth && (
                            <div className={styles.dateChangeNote}>
                                <span className={styles.warningIcon}>⚠️</span>
                                Changing this date will require re-verification
                            </div>
                        )}
                    </div>
                </div>

                <div className={styles.formSection}>
                    <h2 className={styles.sectionTitle}>Contact Information</h2>

                    <Input
                        type="text"
                        placeholder="Enter your phone number"
                        value={formData.phoneNumber || ''}
                        onChange={(value) => handleChange('phoneNumber', value)}
                        disabled={isLoading}
                        label="Phone Number"
                    />

                    <Input
                        type="text"
                        placeholder="Enter your city"
                        value={formData.city || ''}
                        onChange={(value) => handleChange('city', value)}
                        disabled={isLoading}
                        label="City"
                    />
                </div>

                <div className={styles.formActions}>
                    <Button
                        type="button"
                        variant="secondary"
                        onClick={onCancel}
                        disabled={isLoading}
                        style={{ minWidth: '100px' }}
                    >
                        Cancel
                    </Button>
                    <Button
                        type="submit"
                        variant="primary"
                        loading={isLoading}
                        disabled={isLoading}
                        style={{ minWidth: '120px' }}
                    >
                        {isLoading ? 'Updating...' : 'Save Changes'}
                    </Button>
                </div>
            </form>
        </div>
    );
};