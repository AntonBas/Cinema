import { useState, useMemo, useEffect } from 'react';
import { useTicketType } from '@/hooks/features/ticketType/useTicketType';
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
        fetchTicketTypes,
        remove: deleteTicketType,
        toggleActive
    } = useTicketType();

    const filteredTicketTypes = useMemo(() => {
        let filtered = ticketTypes;

        if (statusFilter === 'active') {
            filtered = filtered.filter(t => t.active);
        } else if (statusFilter === 'inactive') {
            filtered = filtered.filter(t => !t.active);
        }

        if (categoryFilter !== 'all') {
            filtered = filtered.filter(t => t.category === categoryFilter);
        }

        if (searchQuery.trim()) {
            const query = searchQuery.toLowerCase();
            filtered = filtered.filter(t =>
                t.code.toLowerCase().includes(query) ||
                t.displayName.toLowerCase().includes(query)
            );
        }

        return filtered;
    }, [ticketTypes, statusFilter, categoryFilter, searchQuery]);

    useEffect(() => {
        fetchTicketTypes({
            statusFilter,
            categoryFilter: categoryFilter === 'all' ? undefined : categoryFilter,
            searchQuery: searchQuery.trim() ? searchQuery : undefined
        });
    }, [statusFilter, categoryFilter, searchQuery]);

    const handleCreateSuccess = () => {
        setShowCreateModal(false);
        fetchTicketTypes({
            statusFilter,
            categoryFilter: categoryFilter === 'all' ? undefined : categoryFilter,
            searchQuery: searchQuery.trim() ? searchQuery : undefined
        });
    };

    const handleEditSuccess = () => {
        setEditingTicketType(null);
        fetchTicketTypes({
            statusFilter,
            categoryFilter: categoryFilter === 'all' ? undefined : categoryFilter,
            searchQuery: searchQuery.trim() ? searchQuery : undefined
        });
    };

    const handleDelete = async (id: number) => {
        try {
            await deleteTicketType(id);
            fetchTicketTypes({
                statusFilter,
                categoryFilter: categoryFilter === 'all' ? undefined : categoryFilter,
                searchQuery: searchQuery.trim() ? searchQuery : undefined
            });
        } catch (err) {
            console.error('Failed to delete ticket type:', err);
        }
    };

    const handleToggleActive = async (id: number) => {
        try {
            await toggleActive(id);
            fetchTicketTypes({
                statusFilter,
                categoryFilter: categoryFilter === 'all' ? undefined : categoryFilter,
                searchQuery: searchQuery.trim() ? searchQuery : undefined
            });
        } catch (err) {
            console.error('Failed to toggle active status:', err);
        }
    };

    const handleEdit = (ticketType: TicketTypeResponse) => {
        setEditingTicketType(ticketType);
    };

    const activeCount = filteredTicketTypes.filter(t => t.active).length;
    const inactiveCount = filteredTicketTypes.filter(t => !t.active).length;

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
                ) : filteredTicketTypes.length === 0 ? (
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