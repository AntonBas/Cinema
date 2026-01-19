import React from 'react';
import { Badge } from '@/components/ui/Badge/Badge';
import type { BonusCardResponse } from '@/types/bonus';
import styles from './BonusCardInfo.module.css';

interface BonusCardInfoProps {
    cardInfo: BonusCardResponse | null;
    loading: boolean;
}

export const BonusCardInfo: React.FC<BonusCardInfoProps> = ({ cardInfo, loading }) => {
    if (loading) {
        return (
            <div className={styles.cardInfo}>
                <div className={styles.loading}>
                    Loading card info...
                </div>
            </div>
        );
    }

    if (!cardInfo) {
        return (
            <div className={styles.cardInfo}>
                <div className={styles.noData}>
                    No card information available
                </div>
            </div>
        );
    }

    const formatDate = (dateString: string | null) => {
        if (!dateString) return 'Not received yet';
        return new Date(dateString).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    };

    return (
        <div className={styles.cardInfo}>
            <h2 className={styles.title}>Bonus Card Information</h2>

            <div className={styles.infoGrid}>
                <div className={styles.infoItem}>
                    <div className={styles.infoLabel}>Current Points</div>
                    <div className={styles.infoValue}>
                        <Badge variant="primary" size="small">
                            {cardInfo.pointsBalance} points
                        </Badge>
                    </div>
                </div>

                <div className={styles.infoItem}>
                    <div className={styles.infoLabel}>Birthday Bonus</div>
                    <div className={styles.infoValue}>
                        <Badge
                            variant={cardInfo.lastBirthdayBonusDate ? 'success' : 'info'}
                            size="small"
                        >
                            {cardInfo.lastBirthdayBonusDate ?
                                `Received on ${formatDate(cardInfo.lastBirthdayBonusDate)}` :
                                'Not received yet'}
                        </Badge>
                    </div>
                </div>

                <div className={styles.infoItem}>
                    <div className={styles.infoLabel}>Welcome Bonus</div>
                    <div className={styles.infoValue}>
                        <Badge
                            variant={cardInfo.welcomeBonusReceived ? 'success' : 'warning'}
                            size="small"
                        >
                            {cardInfo.welcomeBonusReceived ? 'Received' : 'Available'}
                        </Badge>
                    </div>
                </div>
            </div>
        </div>
    );
};