import React from 'react';
import { Button, Badge } from '@/components/ui';
import type { PromotionAdminResponse } from '@/types/promotion';
import styles from './PromotionTable.module.css';

interface PromotionTableProps {
    promotions: PromotionAdminResponse[];
    onEdit: (promotionId: number) => void;
    onDelete: (promotionId: number, title: string) => void;
    getPromotionStatus: (promotion: PromotionAdminResponse) => string;
    getStatusDisplay: (status: string) => string;
}

const PromotionTable: React.FC<PromotionTableProps> = ({
    promotions,
    onEdit,
    onDelete,
    getPromotionStatus,
    getStatusDisplay
}) => {
    const formatDate = (dateString?: string): string => {
        if (!dateString) return 'No date';
        return new Date(dateString).toLocaleDateString('uk-UA');
    };

    const getStatusVariant = (status: string) => {
        switch (status) {
            case 'active': return 'success';
            case 'upcoming': return 'warning';
            case 'expired': return 'error';
            default: return 'info';
        }
    };

    if (promotions.length === 0) {
        return (
            <div className={styles.emptyContainer}>
                <p className={styles.emptyText}>No promotions found</p>
            </div>
        );
    }

    return (
        <div className={styles.tableWrapper}>
            <table className={styles.table}>
                <thead className={styles.tableHead}>
                    <tr>
                        <th className={styles.th}>Title</th>
                        <th className={styles.th}>Bonus Points</th>
                        <th className={styles.th}>Date Range</th>
                        <th className={styles.th}>Status</th>
                        <th className={styles.th}>Actions</th>
                    </tr>
                </thead>
                <tbody className={styles.tableBody}>
                    {promotions.map((promotion) => {
                        const status = getPromotionStatus(promotion);

                        return (
                            <tr key={promotion.id} className={styles.tr}>
                                <td className={styles.td}>
                                    <div className={styles.titleCell}>
                                        <div className={styles.title}>{promotion.title}</div>
                                    </div>
                                </td>
                                <td className={styles.td}>
                                    <span className={styles.points}>
                                        {promotion.bonusPoints} pts
                                    </span>
                                </td>
                                <td className={styles.td}>
                                    <div className={styles.dates}>
                                        <div>{formatDate(promotion.startDate)}</div>
                                        <div className={styles.dateSeparator}>to</div>
                                        <div>{formatDate(promotion.endDate)}</div>
                                    </div>
                                </td>
                                <td className={styles.td}>
                                    <Badge variant={getStatusVariant(status)}>
                                        {getStatusDisplay(status)}
                                    </Badge>
                                </td>
                                <td className={styles.td}>
                                    <div className={styles.actions}>
                                        <Button
                                            variant="secondary"
                                            size="small"
                                            onClick={() => onEdit(promotion.id)}
                                            className={styles.actionButton}
                                        >
                                            Edit
                                        </Button>
                                        <Button
                                            variant="error"
                                            size="small"
                                            onClick={() => onDelete(promotion.id, promotion.title)}
                                            className={styles.actionButton}
                                        >
                                            Delete
                                        </Button>
                                    </div>
                                </td>
                            </tr>
                        );
                    })}
                </tbody>
            </table>
        </div>
    );
};

export default PromotionTable;