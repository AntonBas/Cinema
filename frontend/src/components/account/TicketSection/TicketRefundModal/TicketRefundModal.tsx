import React, { useState } from 'react';
import { Modal, Button } from '@/components/ui';
import { useRefund } from '@/hooks/features/refund/useRefund';
import type { TicketResponse } from '@/types/ticket';
import styles from './TicketRefundModal.module.css';

interface TicketRefundModalProps {
    ticket: TicketResponse | null;
    onClose: () => void;
    onRefundSuccess?: () => void;
}

const REFUND_REASONS = [
    { value: 'change_plans', label: 'Changed my plans' },
    { value: 'double_booking', label: 'Accidental double booking' },
    { value: 'schedule_conflict', label: 'Schedule conflict' },
    { value: 'other', label: 'Other reason' },
];

const formatDateTime = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    });
};

export const TicketRefundModal: React.FC<TicketRefundModalProps> = ({
    ticket,
    onClose,
    onRefundSuccess,
}) => {
    const [acceptedTerms, setAcceptedTerms] = useState(false);
    const [selectedReason, setSelectedReason] = useState<string>('');
    const { processRefund, loading, refundResult } = useRefund();

    if (!ticket) return null;

    const sessionDate = new Date(ticket.sessionTime);
    const hoursUntilSession = (sessionDate.getTime() - Date.now()) / (1000 * 60 * 60);
    const canRequestRefund = ticket.status === 'ACTIVE' && hoursUntilSession > 24;

    const handleSubmit = async () => {
        if (!selectedReason || !acceptedTerms) return;

        const result = await processRefund({
            ticketId: ticket.id,
            reason: selectedReason,
        });

        if (result) {
            onRefundSuccess?.();
        }
    };

    if (!canRequestRefund) {
        return (
            <Modal isOpen={true} onClose={onClose} title="Refund Not Available" size="medium">
                <div className={styles.errorMessage}>
                    <span>⚠️</span>
                    <p>
                        {!hoursUntilSession
                            ? 'Refunds are only available more than 24 hours before the session'
                            : 'Only active tickets can be refunded'}
                    </p>
                </div>
                <div className={styles.modalFooter}>
                    <Button variant="primary" onClick={onClose}>Close</Button>
                </div>
            </Modal>
        );
    }

    if (refundResult) {
        return (
            <Modal isOpen={true} onClose={onClose} title="Refund Submitted" size="medium">
                <div className={styles.successMessage}>
                    <span>✅</span>
                    <h4>Refund Request Submitted</h4>
                    <p>Refund Number: {refundResult.refundNumber}</p>
                    <p>Status: {refundResult.status}</p>
                    <p>Amount: {refundResult.totalAmount} UAH</p>
                </div>
                <div className={styles.modalFooter}>
                    <Button variant="primary" onClick={onClose}>Close</Button>
                </div>
            </Modal>
        );
    }

    return (
        <Modal isOpen={true} onClose={onClose} title="Request Ticket Refund" size="medium">
            <div className={styles.modalBody}>
                <div className={styles.ticketInfo}>
                    <h4>Ticket Information</h4>
                    <p><strong>Movie:</strong> {ticket.movieTitle}</p>
                    <p><strong>Session:</strong> {formatDateTime(ticket.sessionTime)}</p>
                    <p><strong>Hall:</strong> {ticket.hallName}</p>
                    <p><strong>Seat:</strong> Row {ticket.row}, Seat {ticket.seatNumber}</p>
                    <p><strong>Price:</strong> {ticket.price} UAH</p>
                </div>

                <div className={styles.reasonSelection}>
                    <h4>Select Refund Reason</h4>
                    {REFUND_REASONS.map(reason => (
                        <label key={reason.value} className={styles.reasonOption}>
                            <input
                                type="radio"
                                name="refundReason"
                                value={reason.value}
                                checked={selectedReason === reason.value}
                                onChange={e => setSelectedReason(e.target.value)}
                            />
                            {reason.label}
                        </label>
                    ))}
                </div>

                <div className={styles.termsSection}>
                    <ul className={styles.termsList}>
                        <li>Refunds available 24+ hours before session</li>
                        <li>Processing fee may apply</li>
                        <li>Refunds processed within 5-7 business days</li>
                    </ul>
                    <label className={styles.termsAgreement}>
                        <input
                            type="checkbox"
                            checked={acceptedTerms}
                            onChange={e => setAcceptedTerms(e.target.checked)}
                        />
                        I agree to the refund policy terms
                    </label>
                </div>
            </div>

            <div className={styles.modalFooter}>
                <Button variant="secondary" onClick={onClose}>Cancel</Button>
                <Button
                    variant="primary"
                    onClick={handleSubmit}
                    loading={loading}
                    disabled={!selectedReason || !acceptedTerms || loading}
                >
                    Submit Request
                </Button>
            </div>
        </Modal>
    );
};