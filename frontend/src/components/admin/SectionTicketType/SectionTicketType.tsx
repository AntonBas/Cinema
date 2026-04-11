import { useState, useEffect, useCallback } from 'react';
import { useTicketType } from '@/hooks/features/ticketType/useTicketType';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { usePagination } from '@/hooks/common/usePagination';
import { Button } from '@/components/ui/Button/Button';
import { Pagination } from '@/components/ui/Pagination/Pagination';
import TicketTypeTable from './TicketTypeTable/TicketTypeTable';
import TicketTypeFilters from './TicketTypeFilters/TicketTypeFilters';
import TicketTypeFormModal from './TicketTypeModal/TicketTypeFormModal';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import styles from './SectionTicketType.module.css';
import type { TicketTypeResponse, TicketTypeCategory } from '@/types/ticketType';

const SectionTicketType = () => {
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [editingTicketType, setEditingTicketType] = useState<TicketTypeResponse | null>(null);
    const [statusFilter, setStatusFilter] = useState<'all' | 'active' | 'inactive'>('all');
    const [categoryFilter, setCategoryFilter] = useState<TicketTypeCategory | 'all'>('all');

    const { params, setPage } = usePagination({ size: 10 });
    const { ticketTypes: ticketTypesData, pagination, loading, getAll, remove, toggleActive } = useTicketType();
    const showDelayedLoading = useDelayedLoading(loading, { delay: 150, minDisplayTime: 300 });

    const currentPage = params.page ?? 0;
    const pageSize = params.size ?? 10;

    const loadTicketTypes = useCallback(() => {
        getAll({
            page: currentPage,
            size: pageSize,
            active: statusFilter === 'all' ? undefined : statusFilter === 'active',
            category: categoryFilter === 'all' ? undefined : categoryFilter
        });
    }, [getAll, currentPage, pageSize, statusFilter, categoryFilter]);

    useEffect(() => {
        loadTicketTypes();
    }, [loadTicketTypes]);

    const handleCreateSuccess = useCallback(() => {
        setShowCreateModal(false);
        loadTicketTypes();
    }, [loadTicketTypes]);

    const handleEditSuccess = useCallback(() => {
        setEditingTicketType(null);
        loadTicketTypes();
    }, [loadTicketTypes]);

    const handleDelete = useCallback(async (id: number) => {
        await remove(id);
        if (ticketTypesData.length === 1 && currentPage > 0) {
            setPage(currentPage - 1);
        } else {
            loadTicketTypes();
        }
    }, [remove, loadTicketTypes, ticketTypesData.length, currentPage, setPage]);

    const handleToggleActive = useCallback(async (id: number) => {
        await toggleActive(id);
        loadTicketTypes();
    }, [toggleActive, loadTicketTypes]);

    if (showDelayedLoading && !ticketTypesData.length) {
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
                <Button variant="primary" onClick={() => setShowCreateModal(true)}>
                    Create Ticket Type
                </Button>
            </div>

            <div className={styles.content}>
                <TicketTypeFilters
                    onSearchChange={() => { }}
                    statusFilter={statusFilter}
                    onStatusChange={setStatusFilter}
                    categoryFilter={categoryFilter}
                    onCategoryChange={setCategoryFilter}
                />

                {pagination && pagination.totalElements > 0 && (
                    <div className={styles.resultsInfo}>
                        Showing {pagination.number * pagination.size + 1}-
                        {Math.min((pagination.number + 1) * pagination.size, pagination.totalElements)} of{' '}
                        {pagination.totalElements} ticket types
                    </div>
                )}

                {ticketTypesData.length === 0 ? (
                    <div className={styles.empty}>
                        <p>No ticket types found</p>
                    </div>
                ) : (
                    <>
                        <TicketTypeTable
                            ticketTypes={ticketTypesData}
                            onEdit={setEditingTicketType}
                            onDelete={handleDelete}
                            onToggleActive={handleToggleActive}
                            loading={loading}
                        />

                        {pagination && pagination.totalPages > 1 && (
                            <div className={styles.paginationWrapper}>
                                <Pagination
                                    currentPage={pagination.number}
                                    totalPages={pagination.totalPages}
                                    totalElements={pagination.totalElements}
                                    pageSize={pagination.size}
                                    onPageChange={setPage}
                                    variant="pages"
                                    showInfo={false}
                                />
                            </div>
                        )}
                    </>
                )}
            </div>

            {showCreateModal && (
                <TicketTypeFormModal
                    isOpen={showCreateModal}
                    onClose={() => setShowCreateModal(false)}
                    onSuccess={handleCreateSuccess}
                />
            )}

            {editingTicketType && (
                <TicketTypeFormModal
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