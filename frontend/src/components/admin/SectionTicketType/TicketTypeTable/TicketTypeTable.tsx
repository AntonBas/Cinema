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
    onDelete: (id: number) => Promise<void>;
    onToggleActive: (id: number) => Promise<void>;
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
    const [toggleLoading, setToggleLoading] = useState<number | null>(null);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);

    const handleDeleteClick = (ticketType: TicketTypeResponse) => {
        setSelectedTicketType(ticketType);
        setDeleteModalOpen(true);
        setErrorMessage(null);
    };

    const handleConfirmDelete = async () => {
        if (!selectedTicketType) return;

        setIsDeleting(true);
        setErrorMessage(null);
        try {
            await onDelete(selectedTicketType.id);
            setDeleteModalOpen(false);
            setSelectedTicketType(null);
        } catch (error) {
            setErrorMessage(error instanceof Error ? error.message : 'Failed to delete ticket type');
        } finally {
            setIsDeleting(false);
        }
    };

    const handleToggleActive = async (id: number) => {
        setToggleLoading(id);
        setErrorMessage(null);
        try {
            await onToggleActive(id);
        } catch (error) {
            setErrorMessage(error instanceof Error ? error.message : 'Failed to toggle status');
        } finally {
            setToggleLoading(null);
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
            {errorMessage && (
                <div className={styles.errorMessage}>
                    {errorMessage}
                </div>
            )}

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
                                            onClick={() => handleToggleActive(ticketType.id)}
                                            disabled={toggleLoading === ticketType.id}
                                        >
                                            {toggleLoading === ticketType.id ? 'Updating...' :
                                                ticketType.active ? 'Active' : 'Inactive'}
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
                    setErrorMessage(null);
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