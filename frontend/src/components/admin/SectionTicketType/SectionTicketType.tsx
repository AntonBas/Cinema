import { useState, useMemo, useEffect } from 'react';
import { useTicketType } from '@/hooks/features/ticketType/useTicketType';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { Button } from '@/components/ui/Button/Button';
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
        getAll,
        remove: deleteTicketType,
        toggleActive
    } = useTicketType();

    const showDelayedLoading = useDelayedLoading(loading, { delay: 150, minDisplayTime: 300 });

    const loadTicketTypes = async () => {
        try {
            await getAll({
                active: statusFilter === 'all' ? undefined : statusFilter === 'active',
                category: categoryFilter === 'all' ? undefined : categoryFilter,
                search: searchQuery.trim() || undefined
            });
        } catch (error) {
            console.error('Failed to load ticket types:', error);
        }
    };

    useEffect(() => {
        loadTicketTypes();
    }, [statusFilter, categoryFilter, searchQuery]);

    const filteredTicketTypes = useMemo(() => {
        return ticketTypes || [];
    }, [ticketTypes]);

    const handleCreateSuccess = () => {
        setShowCreateModal(false);
        loadTicketTypes();
    };

    const handleEditSuccess = () => {
        setEditingTicketType(null);
        loadTicketTypes();
    };

    const handleDelete = async (id: number) => {
        try {
            await deleteTicketType(id);
            loadTicketTypes();
        } catch (err) {
            console.error('Failed to delete ticket type:', err);
        }
    };

    const handleToggleActive = async (id: number) => {
        try {
            await toggleActive(id);
            loadTicketTypes();
        } catch (err) {
            console.error('Failed to toggle active status:', err);
        }
    };

    const handleEdit = (ticketType: TicketTypeResponse) => {
        setEditingTicketType(ticketType);
    };

    const activeCount = filteredTicketTypes.filter(t => t.active).length;
    const inactiveCount = filteredTicketTypes.filter(t => !t.active).length;

    if (showDelayedLoading && !filteredTicketTypes.length) {
        return (
            <div className={styles.loading}>
                <LoadingSpinner text="Loading ticket types..." />
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
                    categoryFilter={categoryFilter}
                    onCategoryChange={setCategoryFilter}
                    activeCount={activeCount}
                    inactiveCount={inactiveCount}
                />

                {filteredTicketTypes.length === 0 ? (
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
                            <span>Total: {filteredTicketTypes.length}</span>
                            <span>Active: {activeCount}</span>
                            <span>Inactive: {inactiveCount}</span>
                        </div>
                        <TicketTypeTable
                            ticketTypes={filteredTicketTypes}
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