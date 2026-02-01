import React, { useState } from 'react';
import { Modal, Button, Notification } from '@/components/ui';
import { useRefund } from '@/hooks/features/refund/useRefund';
import type { TicketResponse } from '@/types/ticket';
import { AlertCircle, Clock, DollarSign, ExternalLink, X, Check } from 'lucide-react';
import styles from './TicketRefundModal.module.css';

interface TicketRefundModalProps {
    ticket: TicketResponse | null;
    onClose: () => void;
    onRefundSuccess?: () => void;
}

export const TicketRefundModal: React.FC<TicketRefundModalProps> = ({
    ticket,
    onClose,
    onRefundSuccess
}) => {
    const [acceptedTerms, setAcceptedTerms] = useState(false);
    const [selectedReason, setSelectedReason] = useState<string>('');
    const [notification, setNotification] = useState<{ type: 'success' | 'error', message: string } | null>(null);

    const {
        processRefund,
        loading,
        refundResult
    } = useRefund();

    if (!ticket) return null;

    const formatDateTime = (dateString: string) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const refundReasons = [
        { value: 'change_plans', label: 'Changed my plans' },
        { value: 'double_booking', label: 'Accidental double booking' },
        { value: 'schedule_conflict', label: 'Schedule conflict' },
        { value: 'technical_issue', label: 'Technical issue during purchase' },
        { value: 'other', label: 'Other reason' }
    ];

    const handleSubmit = async () => {
        if (!ticket || !selectedReason) return;

        if (!acceptedTerms) {
            setNotification({ type: 'error', message: 'You must accept the refund policy terms' });
            return;
        }

        const refundRequest = {
            ticketId: ticket.id,
            reason: selectedReason
        };

        try {
            const result = await processRefund(refundRequest);
            if (result) {
                setNotification({ type: 'success', message: 'Refund request submitted successfully' });
                setTimeout(() => {
                    onClose();
                    onRefundSuccess?.();
                }, 2000);
            }
        } catch (err) {
            setNotification({ type: 'error', message: 'Failed to submit refund request' });
        }
    };

    const sessionDate = new Date(ticket.sessionTime);
    const now = new Date();
    const hoursUntilSession = (sessionDate.getTime() - now.getTime()) / (1000 * 60 * 60);
    const isWithinRefundWindow = hoursUntilSession > 24;
    const canRequestRefund = ticket.status === 'ACTIVE' && isWithinRefundWindow;

    return (
        <Modal
            isOpen={!!ticket}
            onClose={onClose}
            title="Request Ticket Refund"
            size="medium"
        >
            {notification && (
                <Notification
                    id="refund-notification"
                    message={notification.message}
                    type={notification.type}
                    isVisible={true}
                    onClose={() => setNotification(null)}
                    duration={3000}
                    position={0}
                />
            )}

            <div className={styles.modalBody}>
                {!canRequestRefund ? (
                    <div className={styles.errorMessage}>
                        <AlertCircle size={24} />
                        <div>
                            <h4>Refund Not Available</h4>
                            <p>
                                {!isWithinRefundWindow
                                    ? 'Refunds are only available more than 24 hours before the session'
                                    : 'Only active tickets can be refunded'}
                            </p>
                        </div>
                    </div>
                ) : refundResult ? (
                    <div className={styles.successMessage}>
                        <Check size={24} />
                        <div>
                            <h4>Refund Request Submitted</h4>
                            <p>Your refund request has been submitted for review.</p>
                            <div className={styles.refundDetails}>
                                <div className={styles.detailItem}>
                                    <span className={styles.detailLabel}>Refund Number:</span>
                                    <span className={styles.detailValue}>{refundResult.refundNumber}</span>
                                </div>
                                <div className={styles.detailItem}>
                                    <span className={styles.detailLabel}>Status:</span>
                                    <span className={`${styles.detailValue} ${styles.status}`}>
                                        {refundResult.status}
                                    </span>
                                </div>
                                <div className={styles.detailItem}>
                                    <span className={styles.detailLabel}>Amount:</span>
                                    <span className={`${styles.detailValue} ${styles.amount}`}>
                                        {refundResult.totalAmount} UAH
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                ) : (
                    <>
                        <div className={styles.ticketInfo}>
                            <h4>Ticket Information</h4>
                            <div className={styles.infoGrid}>
                                <div className={styles.infoItem}>
                                    <span className={styles.infoLabel}>Movie:</span>
                                    <span className={styles.infoValue}>{ticket.movieTitle}</span>
                                </div>
                                <div className={styles.infoItem}>
                                    <span className={styles.infoLabel}>Session:</span>
                                    <span className={styles.infoValue}>{formatDateTime(ticket.sessionTime)}</span>
                                </div>
                                <div className={styles.infoItem}>
                                    <span className={styles.infoLabel}>Hall:</span>
                                    <span className={styles.infoValue}>{ticket.hallName}</span>
                                </div>
                                <div className={styles.infoItem}>
                                    <span className={styles.infoLabel}>Seat:</span>
                                    <span className={styles.infoValue}>
                                        Row {ticket.row}, Seat {ticket.seatNumber}
                                    </span>
                                </div>
                                <div className={styles.infoItem}>
                                    <span className={styles.infoLabel}>Price:</span>
                                    <span className={styles.infoValue}>{ticket.price} UAH</span>
                                </div>
                            </div>
                        </div>

                        <div className={styles.refundDetails}>
                            <h4>Refund Information</h4>
                            <div className={styles.refundEstimate}>
                                <DollarSign size={20} />
                                <div>
                                    <span className={styles.estimateLabel}>Estimated Refund:</span>
                                    <span className={styles.estimateValue}>
                                        {ticket.price} UAH
                                    </span>
                                    <small className={styles.estimateNote}>
                                        (Processing fee may apply)
                                    </small>
                                </div>
                            </div>
                            <div className={styles.timeWarning}>
                                <Clock size={20} />
                                <span>
                                    Refund will be processed within 5-7 business days
                                </span>
                            </div>
                        </div>

                        <div className={styles.reasonSelection}>
                            <h4>Select Refund Reason</h4>
                            <div className={styles.reasonOptions}>
                                {refundReasons.map(reason => (
                                    <label key={reason.value} className={styles.reasonOption}>
                                        <input
                                            type="radio"
                                            name="refundReason"
                                            value={reason.value}
                                            checked={selectedReason === reason.value}
                                            onChange={(e) => setSelectedReason(e.target.value)}
                                            className={styles.reasonInput}
                                        />
                                        <span className={styles.reasonLabel}>{reason.label}</span>
                                    </label>
                                ))}
                            </div>
                            {selectedReason === 'other' && (
                                <div className={styles.otherReason}>
                                    <textarea
                                        placeholder="Please specify your reason..."
                                        className={styles.otherTextarea}
                                        maxLength={500}
                                    />
                                    <small className={styles.charCount}>Max 500 characters</small>
                                </div>
                            )}
                        </div>

                        <div className={styles.termsSection}>
                            <div className={styles.termsHeader}>
                                <h4>Refund Policy</h4>
                                <a
                                    href="/refund-policy"
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className={styles.policyLink}
                                >
                                    View full policy <ExternalLink size={14} />
                                </a>
                            </div>
                            <div className={styles.termsContent}>
                                <ul className={styles.termsList}>
                                    <li>Refunds are only available more than 24 hours before the session</li>
                                    <li>A processing fee of 10% may apply</li>
                                    <li>Refunds are processed within 5-7 business days</li>
                                    <li>Refunded amount will be returned to your original payment method</li>
                                    <li>Refund requests are subject to review and approval</li>
                                </ul>
                            </div>
                            <label className={styles.termsAgreement}>
                                <input
                                    type="checkbox"
                                    checked={acceptedTerms}
                                    onChange={(e) => setAcceptedTerms(e.target.checked)}
                                    className={styles.termsCheckbox}
                                />
                                <span className={styles.termsText}>
                                    I have read and agree to the refund policy terms
                                </span>
                            </label>
                        </div>
                    </>
                )}
            </div>

            <div className={styles.modalFooter}>
                {refundResult ? (
                    <Button variant="primary" onClick={onClose}>
                        Close
                    </Button>
                ) : (
                    <>
                        <Button variant="cancel" onClick={onClose}>
                            <X size={18} /> Cancel
                        </Button>
                        <Button
                            variant="primary"
                            onClick={handleSubmit}
                            loading={loading}
                            disabled={!selectedReason || !acceptedTerms || loading || !canRequestRefund}
                        >
                            <Check size={18} /> Submit Refund Request
                        </Button>
                    </>
                )}
            </div>
        </Modal>
    );
};