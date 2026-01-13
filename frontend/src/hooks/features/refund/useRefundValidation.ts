import { useCallback } from 'react';
import type { RefundPreviewResponse } from '@/types/refund';

export const useRefundValidation = () => {
    const validateRefundEligibility = useCallback((preview: RefundPreviewResponse | null): {
        isValid: boolean;
        errors: string[];
        warnings: string[];
    } => {
        const errors: string[] = [];
        const warnings: string[] = [];

        if (!preview) {
            errors.push('No refund preview available');
            return { isValid: false, errors, warnings };
        }

        if (!preview.isRefundable) {
            errors.push(preview.nonRefundableReason || 'This ticket is not refundable');
        }

        if (preview.refundDeadline) {
            const deadline = new Date(preview.refundDeadline);
            const now = new Date();

            if (now > deadline) {
                errors.push('Refund deadline has passed');
            }
        }

        const refundAmount = parseFloat(preview.refundAmount) || 0;
        const feeAmount = parseFloat(preview.feeAmount) || 0;
        const netAmount = refundAmount - feeAmount;

        if (netAmount <= 0) {
            errors.push('Refund amount after fees is zero or negative');
        }

        if (feeAmount > 0) {
            warnings.push(`Processing fee: ${feeAmount} UAH will be deducted`);
        }

        if (preview.bonusPointsUsed > 0 && preview.bonusPointsToRefund < preview.bonusPointsUsed) {
            warnings.push(`Only ${preview.bonusPointsToRefund} of ${preview.bonusPointsUsed} bonus points will be refunded`);
        }

        return {
            isValid: errors.length === 0 && preview.isRefundable,
            errors,
            warnings
        };
    }, []);

    const calculateNetRefund = useCallback((refundAmount: string, feeAmount: string): number => {
        const amount = parseFloat(refundAmount) || 0;
        const fee = parseFloat(feeAmount) || 0;
        return Math.max(0, amount - fee);
    }, []);

    const calculateRefundPercentage = useCallback((refundAmount: string, originalPrice: string): number => {
        const refund = parseFloat(refundAmount) || 0;
        const original = parseFloat(originalPrice) || 0;

        if (original <= 0) return 0;
        return (refund / original) * 100;
    }, []);

    const getTimeUntilDeadline = useCallback((deadline?: string): string | null => {
        if (!deadline) return null;

        const deadlineDate = new Date(deadline);
        const now = new Date();
        const diffMs = deadlineDate.getTime() - now.getTime();

        if (diffMs <= 0) return 'Expired';

        const diffMinutes = Math.floor(diffMs / (1000 * 60));
        const diffHours = Math.floor(diffMinutes / 60);
        const diffDays = Math.floor(diffHours / 24);

        if (diffDays > 0) {
            return `${diffDays} days, ${diffHours % 24} hours`;
        } else if (diffHours > 0) {
            return `${diffHours} hours, ${diffMinutes % 60} minutes`;
        } else {
            return `${diffMinutes} minutes`;
        }
    }, []);

    const isDeadlineApproaching = useCallback((deadline?: string, thresholdHours: number = 24): boolean => {
        if (!deadline) return false;

        const deadlineDate = new Date(deadline);
        const now = new Date();
        const diffMs = deadlineDate.getTime() - now.getTime();
        const diffHours = Math.floor(diffMs / (1000 * 60 * 60));

        return diffHours > 0 && diffHours <= thresholdHours;
    }, []);

    const formatRefundPolicy = useCallback((preview: RefundPreviewResponse | null): {
        name: string;
        description: string;
        keyPoints: string[];
    } => {
        if (!preview) {
            return {
                name: 'No policy available',
                description: '',
                keyPoints: []
            };
        }

        const keyPoints: string[] = [];

        if (preview.refundPercentage) {
            const percentage = parseFloat(preview.refundPercentage);
            keyPoints.push(`Refund percentage: ${percentage}%`);
        }

        if (preview.feePercentage) {
            const feePercentage = parseFloat(preview.feePercentage);
            keyPoints.push(`Processing fee: ${feePercentage}%`);
        }

        if (preview.refundDeadline) {
            keyPoints.push(`Refund deadline: ${new Date(preview.refundDeadline).toLocaleDateString()}`);
        }

        return {
            name: preview.policyName || 'Standard Refund Policy',
            description: preview.policyDescription || '',
            keyPoints
        };
    }, []);

    const validateRefundReason = useCallback((reason?: string): {
        isValid: boolean;
        error?: string;
        warning?: string;
    } => {
        if (!reason || reason.trim().length === 0) {
            return {
                isValid: true,
                warning: 'Providing a reason will help us in the future'
            };
        }

        if (reason.trim().length < 5) {
            return {
                isValid: false,
                error: 'Reason must be at least 5 characters long'
            };
        }

        if (reason.length > 500) {
            return {
                isValid: false,
                error: 'Reason must be less than 500 characters'
            };
        }

        return { isValid: true };
    }, []);

    return {
        validateRefundEligibility,
        calculateNetRefund,
        calculateRefundPercentage,
        getTimeUntilDeadline,
        isDeadlineApproaching,
        formatRefundPolicy,
        validateRefundReason
    };
};