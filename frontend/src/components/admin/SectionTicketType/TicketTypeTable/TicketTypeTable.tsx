import React, { useState } from 'react';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Tooltip } from '@/components/ui/Tooltip';
import { DeleteConfirmModal } from '@/components/ui/DeleteConfirmModal';
import type { TicketTypeResponse } from '@/types/ticketType';
import { TicketTypeCategoryDisplay } from '@/types/ticketType';
import styles from './TicketTypeTable.module.css';

interface TicketTypeTableProps {
    ticketTypes: TicketTypeResponse[];
    onEdit: (ticketType: TicketTypeResponse) => void;
    onDelete: (id: number) => void;
    onToggleActive: (id: number) => void;
}

const TicketTypeTable: React.FC<TicketTypeTableProps> = ({
    ticketTypes,
    onEdit,
    onDelete,
    onToggleActive
}) => {
    const [deleteModalOpen, setDeleteModalOpen] = useState(false);
    const [selectedTicketType, setSelectedTicketType] = useState<TicketTypeResponse | null>(null);
    const [isDeleting, setIsDeleting] = useState(false);

    const handleDeleteClick = (ticketType: TicketTypeResponse) => {
        setSelectedTicketType(ticketType);
        setDeleteModalOpen(true);
    };

    const handleConfirmDelete = async () => {
        if (!selectedTicketType) return;

        setIsDeleting(true);
        try {
            await onDelete(selectedTicketType.id);
            setDeleteModalOpen(false);
            setSelectedTicketType(null);
        } finally {
            setIsDeleting(false);
        }
    };

    const formatAgeRange = (ticketType: TicketTypeResponse) => {
        const { minAge, maxAge } = ticketType;
        if (minAge === null && maxAge === null) return 'Any age';
        if (minAge !== null && maxAge !== null) return `${minAge}-${maxAge} years`;
        if (minAge !== null) return `≥ ${minAge} years`;
        if (maxAge !== null) return `≤ ${maxAge} years`;
        return 'Any age';
    };

    const getCategoryVariant = (category: string) => {
        switch (category) {
            case 'STANDARD': return 'primary';
            case 'CHILD': return 'secondary';
            case 'STUDENT': return 'info';
            case 'DISABLED': return 'warning';
            case 'MILITARY': return 'error';
            case 'SENIOR': return 'success';
            default: return 'outline';
        }
    };

    if (ticketTypes.length === 0) {
        return (
            <div className={styles.empty}>
                <div className={styles.emptyIcon}>🎫</div>
                <h3>No ticket types found</h3>
                <p>There are no ticket types matching your criteria.</p>
            </div>
        );
    }

    return (
        <>
            <div className={styles.tableWrapper}>
                <div className={styles.tableContainer}>
                    <table className={styles.table}>
                        <thead>
                            <tr>
                                <th>Display Name</th>
                                <th>Category</th>
                                <th>Price Multiplier</th>
                                <th>Age Range</th>
                                <th>Document</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {ticketTypes.map((ticketType) => (
                                <tr key={ticketType.id} className={styles.row}>
                                    <td className={styles.nameCell}>
                                        <span className={styles.displayName}>{ticketType.displayName}</span>
                                    </td>
                                    <td className={styles.categoryCell}>
                                        <Badge variant={getCategoryVariant(ticketType.category)}>
                                            {TicketTypeCategoryDisplay[ticketType.category]}
                                        </Badge>
                                    </td>
                                    <td className={styles.priceCell}>
                                        <span className={styles.price}>× {ticketType.priceMultiplier}</span>
                                    </td>
                                    <td className={styles.ageCell}>
                                        <span className={styles.ageRange}>
                                            {formatAgeRange(ticketType)}
                                        </span>
                                    </td>
                                    <td className={styles.documentCell}>
                                        {ticketType.requiresDocument ? (
                                            <Tooltip content={ticketType.documentType || 'Document required'}>
                                                <Badge variant="warning">Required</Badge>
                                            </Tooltip>
                                        ) : (
                                            <Badge variant="outline">Not required</Badge>
                                        )}
                                    </td>
                                    <td className={styles.statusCell}>
                                        <button
                                            className={`${styles.toggleButton} ${ticketType.active ? styles.activeToggle : styles.inactiveToggle
                                                }`}
                                            onClick={() => onToggleActive(ticketType.id)}
                                        >
                                            {ticketType.active ? 'Active' : 'Inactive'}
                                        </button>
                                    </td>
                                    <td className={styles.actionsCell}>
                                        <div className={styles.actions}>
                                            <Button
                                                variant="secondary"
                                                size="small"
                                                onClick={() => onEdit(ticketType)}
                                                className={styles.actionButton}
                                            >
                                                Edit
                                            </Button>
                                            <Button
                                                variant="error"
                                                size="small"
                                                onClick={() => handleDeleteClick(ticketType)}
                                                className={styles.actionButton}
                                            >
                                                Delete
                                            </Button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>

            <DeleteConfirmModal
                isOpen={deleteModalOpen}
                onConfirm={handleConfirmDelete}
                onCancel={() => {
                    setDeleteModalOpen(false);
                    setSelectedTicketType(null);
                }}
                itemName={selectedTicketType?.displayName}
                itemType="ticket type"
                isDeleting={isDeleting}
                title="Delete Ticket Type"
                message="Are you sure you want to delete this ticket type?"
                confirmText="Delete Ticket Type"
            />
        </>
    );
};

export default TicketTypeTable;