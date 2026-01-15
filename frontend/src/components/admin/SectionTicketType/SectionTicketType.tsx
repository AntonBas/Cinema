import { useState } from 'react';
import { useTicketTypeList } from '@/hooks/features/ticketType/useTicketTypeList';
import { Button } from '@/components/ui/Button';
import TicketTypeTable from './TicketTypeTable/TicketTypeTable';
import TicketTypeFilters from './TicketTypeFilters/TicketTypeFilters';
import CreateTicketTypeModal from './TicketTypeModal/CreateTicketTypeModal';
import EditTicketTypeModal from './TicketTypeModal/EditTicketTypeModal';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import styles from './SectionTicketType.module.css';
import type { TicketTypeResponse } from '@/types/ticketType';

const SectionTicketType = () => {
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [editingTicketType, setEditingTicketType] = useState<TicketTypeResponse | null>(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [statusFilter, setStatusFilter] = useState<'all' | 'active' | 'inactive'>('all');

    const {
        ticketTypes,
        loading,
        error,
        refresh,
        removeTicketType,
        toggleTicketTypeActive
    } = useTicketTypeList({
        activeFilter: statusFilter === 'all' ? undefined : statusFilter === 'active',
        autoFetch: true
    });

    const filteredTicketTypes = ticketTypes.filter(ticketType =>
        ticketType.displayName.toLowerCase().includes(searchQuery.toLowerCase()) ||
        ticketType.code.toLowerCase().includes(searchQuery.toLowerCase()) ||
        ticketType.category.toLowerCase().includes(searchQuery.toLowerCase())
    );

    const handleCreateSuccess = () => {
        setShowCreateModal(false);
        refresh();
    };

    const handleEditSuccess = () => {
        setEditingTicketType(null);
        refresh();
    };

    const handleDelete = async (id: number) => {
        await removeTicketType(id);
    };

    const handleToggleActive = async (id: number) => {
        await toggleTicketTypeActive(id);
    };

    const handleEdit = (ticketType: TicketTypeResponse) => {
        setEditingTicketType(ticketType);
    };

    if (error) {
        return (
            <div className={styles.section}>
                <div className={styles.error}>
                    <h3>Error loading ticket types</h3>
                    <p>{error}</p>
                    <Button onClick={() => refresh()}>
                        Try Again
                    </Button>
                </div>
            </div>
        );
    }

    return (
        <div className={styles.section}>
            <div className={styles.header}>
                <h1 className={styles.title}>Ticket Types</h1>
                <div className={styles.actions}>
                    <Button
                        variant="primary"
                        onClick={() => setShowCreateModal(true)}
                    >
                        Create Ticket Type
                    </Button>
                </div>
            </div>

            <div className={styles.content}>
                <TicketTypeFilters
                    searchQuery={searchQuery}
                    onSearchChange={setSearchQuery}
                    statusFilter={statusFilter}
                    onStatusChange={setStatusFilter}
                />

                {loading ? (
                    <div className={styles.loading}>
                        <LoadingSpinner text="Loading ticket types..." />
                    </div>
                ) : filteredTicketTypes.length === 0 ? (
                    <div className={styles.empty}>
                        <p>No ticket types found</p>
                        {searchQuery && (
                            <Button
                                variant="secondary"
                                onClick={() => setSearchQuery('')}
                            >
                                Clear search
                            </Button>
                        )}
                    </div>
                ) : (
                    <TicketTypeTable
                        ticketTypes={filteredTicketTypes}
                        onEdit={handleEdit}
                        onDelete={handleDelete}
                        onToggleActive={handleToggleActive}
                    />
                )}
            </div>

            {showCreateModal && (
                <CreateTicketTypeModal
                    isOpen={showCreateModal}
                    onClose={() => setShowCreateModal(false)}
                    onSuccess={handleCreateSuccess}
                />
            )}

            {editingTicketType && (
                <EditTicketTypeModal
                    isOpen={!!editingTicketType}
                    onClose={() => setEditingTicketType(null)}
                    onSuccess={handleEditSuccess}
                    ticketType={editingTicketType}
                />
            )}
        </div>
    );
};

export default SectionTicketType;