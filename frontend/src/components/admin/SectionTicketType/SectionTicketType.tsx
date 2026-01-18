import { useState } from 'react';
import { useTicketTypeList } from '@/hooks/features/ticketType/useTicketTypeList';
import { Button } from '@/components/ui/Button';
import TicketTypeTable from './TicketTypeTable/TicketTypeTable';
import TicketTypeFilters from './TicketTypeFilters/TicketTypeFilters';
import CreateTicketTypeModal from './TicketTypeModal/CreateTicketTypeModal';
import EditTicketTypeModal from './TicketTypeModal/EditTicketTypeModal';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import styles from './SectionTicketType.module.css';
import type { TicketTypeResponse, TicketTypeCategory } from '@/types/ticketType';

const SectionTicketType = () => {
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [editingTicketType, setEditingTicketType] = useState<TicketTypeResponse | null>(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [statusFilter, setStatusFilter] = useState<'all' | 'active' | 'inactive'>('all');
    const [categoryFilter, setCategoryFilter] = useState<TicketTypeCategory | 'all'>('all');

    const {
        ticketTypes,
        loading,
        error,
        refresh,
        removeTicketType,
        toggleTicketTypeActive
    } = useTicketTypeList({
        statusFilter,
        categoryFilter,
        searchQuery,
        autoFetch: true
    });

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

    const activeCount = ticketTypes.filter(t => t.active).length;
    const inactiveCount = ticketTypes.filter(t => !t.active).length;

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
                    categoryFilter={categoryFilter}
                    onCategoryChange={setCategoryFilter}
                    activeCount={activeCount}
                    inactiveCount={inactiveCount}
                />

                {loading ? (
                    <div className={styles.loading}>
                        <LoadingSpinner text="Loading ticket types..." />
                    </div>
                ) : ticketTypes.length === 0 ? (
                    <div className={styles.empty}>
                        <p>No ticket types found</p>
                        {(searchQuery || statusFilter !== 'all' || categoryFilter !== 'all') && (
                            <Button
                                variant="secondary"
                                onClick={() => {
                                    setSearchQuery('');
                                    setStatusFilter('all');
                                    setCategoryFilter('all');
                                }}
                            >
                                Clear all filters
                            </Button>
                        )}
                    </div>
                ) : (
                    <>
                        <div className={styles.stats}>
                            <span>Total: {ticketTypes.length}</span>
                            <span>Active: {activeCount}</span>
                            <span>Inactive: {inactiveCount}</span>
                        </div>
                        <TicketTypeTable
                            ticketTypes={ticketTypes}
                            onEdit={handleEdit}
                            onDelete={handleDelete}
                            onToggleActive={handleToggleActive}
                        />
                    </>
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