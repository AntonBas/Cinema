import { useState, useMemo, useEffect, useCallback, useRef } from 'react';
import { useTicketType } from '@/hooks/features/ticketType/useTicketType';
import { useNotification } from '@/hooks/common/useNotification';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { Button } from '@/components/ui/Button/Button';
import { Notification } from '@/components/ui/Notification/Notification';
import TicketTypeTable from './TicketTypeTable/TicketTypeTable';
import TicketTypeFilters from './TicketTypeFilters/TicketTypeFilters';
import TicketTypeFormModal from './TicketTypeModal/TicketTypeFormModal';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import styles from './SectionTicketType.module.css';
import type { TicketTypeResponse, TicketTypeCategory } from '@/types/ticketType';

const SectionTicketType = () => {
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [editingTicketType, setEditingTicketType] = useState<TicketTypeResponse | null>(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [statusFilter, setStatusFilter] = useState<'all' | 'active' | 'inactive'>('all');
    const [categoryFilter, setCategoryFilter] = useState<TicketTypeCategory | 'all'>('all');
    const [page, setPage] = useState(0);
    const [rowsPerPage] = useState(10);

    const isInitialMount = useRef(true);
    const isLoadingRef = useRef(false);
    const prevParamsRef = useRef({ statusFilter, categoryFilter, searchQuery, page });

    const {
        ticketTypes,
        loading,
        getAll,
        remove: deleteTicketType,
        toggleActive
    } = useTicketType();

    const { notifications, showNotification, hideNotification } = useNotification();
    const showDelayedLoading = useDelayedLoading(loading, { delay: 150, minDisplayTime: 300 });

    const loadTicketTypes = useCallback(async (
        params: {
            status: 'all' | 'active' | 'inactive';
            category: TicketTypeCategory | 'all';
            search: string;
            page: number;
        },
        skipCache = true
    ) => {
        if (isLoadingRef.current) {
            return;
        }

        isLoadingRef.current = true;
        try {
            await getAll({
                active: params.status === 'all' ? undefined : params.status === 'active',
                category: params.category === 'all' ? undefined : params.category,
                search: params.search.trim() || undefined,
                page: params.page,
                size: rowsPerPage
            }, skipCache);
        } catch (error) {
            showNotification('Failed to load ticket types', 'error');
        } finally {
            isLoadingRef.current = false;
        }
    }, [getAll, rowsPerPage, showNotification]);

    useEffect(() => {
        loadTicketTypes({
            status: statusFilter,
            category: categoryFilter,
            search: searchQuery,
            page
        }, false);
    }, []);

    useEffect(() => {
        if (isInitialMount.current) {
            isInitialMount.current = false;
            return;
        }

        const currentParams = { statusFilter, categoryFilter, searchQuery, page };
        const prevParams = prevParamsRef.current;

        const paramsChanged =
            prevParams.statusFilter !== currentParams.statusFilter ||
            prevParams.categoryFilter !== currentParams.categoryFilter ||
            prevParams.searchQuery !== currentParams.searchQuery ||
            prevParams.page !== currentParams.page;

        if (!paramsChanged) {
            return;
        }

        prevParamsRef.current = currentParams;

        const timer = setTimeout(() => {
            loadTicketTypes({
                status: statusFilter,
                category: categoryFilter,
                search: searchQuery,
                page
            }, true);
        }, 100);

        return () => clearTimeout(timer);
    }, [statusFilter, categoryFilter, searchQuery, page, loadTicketTypes]);

    const ticketTypeList = useMemo(() => {
        return ticketTypes?.content || [];
    }, [ticketTypes]);

    const handleCreateSuccess = useCallback(async (newTicketType?: TicketTypeResponse) => {
        setShowCreateModal(false);
        if (newTicketType) {
            showNotification(`Ticket type "${newTicketType.displayName}" created successfully!`, 'success');
        } else {
            showNotification('Ticket type created successfully!', 'success');
        }
        await loadTicketTypes({
            status: statusFilter,
            category: categoryFilter,
            search: searchQuery,
            page
        }, true);
    }, [loadTicketTypes, showNotification, statusFilter, categoryFilter, searchQuery, page]);

    const handleEditSuccess = useCallback(async (updatedTicketType?: TicketTypeResponse) => {
        setEditingTicketType(null);
        if (updatedTicketType) {
            showNotification(`Ticket type "${updatedTicketType.displayName}" updated successfully!`, 'success');
        } else {
            showNotification('Ticket type updated successfully!', 'success');
        }
        await loadTicketTypes({
            status: statusFilter,
            category: categoryFilter,
            search: searchQuery,
            page
        }, true);
    }, [loadTicketTypes, showNotification, statusFilter, categoryFilter, searchQuery, page]);

    const handleDelete = useCallback(async (id: number, displayName: string) => {
        try {
            await deleteTicketType(id);
            showNotification(`Ticket type "${displayName}" deleted successfully!`, 'success');
            await loadTicketTypes({
                status: statusFilter,
                category: categoryFilter,
                search: searchQuery,
                page
            }, true);
        } catch (err) {
            showNotification('Failed to delete ticket type', 'error');
        }
    }, [deleteTicketType, loadTicketTypes, showNotification, statusFilter, categoryFilter, searchQuery, page]);

    const handleToggleActive = useCallback(async (id: number, displayName: string) => {
        try {
            const updated = await toggleActive(id);
            const status = updated?.active ? 'activated' : 'deactivated';
            showNotification(`Ticket type "${displayName}" ${status} successfully!`, 'success');
            await loadTicketTypes({
                status: statusFilter,
                category: categoryFilter,
                search: searchQuery,
                page
            }, true);
        } catch (err) {
            showNotification('Failed to toggle active status', 'error');
        }
    }, [toggleActive, loadTicketTypes, showNotification, statusFilter, categoryFilter, searchQuery, page]);

    const handleEdit = useCallback((ticketType: TicketTypeResponse) => {
        setEditingTicketType(ticketType);
    }, []);

    const handlePageChange = useCallback((newPage: number) => {
        setPage(newPage);
    }, []);

    const handleClearFilters = useCallback(() => {
        setSearchQuery('');
        setStatusFilter('all');
        setCategoryFilter('all');
        setPage(0);
    }, []);

    const activeCount = useMemo(() => {
        return ticketTypeList.filter(t => t.active).length;
    }, [ticketTypeList]);

    const inactiveCount = useMemo(() => {
        return ticketTypeList.filter(t => !t.active).length;
    }, [ticketTypeList]);

    if (showDelayedLoading && !ticketTypeList.length) {
        return (
            <div className={styles.loading}>
                <LoadingSpinner text="Loading ticket types..." />
            </div>
        );
    }

    return (
        <div className={styles.section}>
            {notifications.map((notification) => (
                <Notification
                    key={notification.id}
                    id={notification.id}
                    message={notification.message}
                    type={notification.type}
                    isVisible={notification.isVisible}
                    onClose={hideNotification}
                    duration={notification.duration}
                />
            ))}

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
                />

                {ticketTypeList.length === 0 ? (
                    <div className={styles.empty}>
                        <p>No ticket types found</p>
                        {(searchQuery || statusFilter !== 'all' || categoryFilter !== 'all') && (
                            <Button
                                variant="secondary"
                                onClick={handleClearFilters}
                            >
                                Clear all filters
                            </Button>
                        )}
                    </div>
                ) : (
                    <>
                        <div className={styles.stats}>
                            <span>Total: {ticketTypes?.totalElements || ticketTypeList.length}</span>
                            <span>Active: {activeCount}</span>
                            <span>Inactive: {inactiveCount}</span>
                        </div>
                        <TicketTypeTable
                            ticketTypes={ticketTypeList}
                            onEdit={handleEdit}
                            onDelete={handleDelete}
                            onToggleActive={handleToggleActive}
                        />

                        {ticketTypes && ticketTypes.totalPages > 1 && (
                            <div className={styles.pagination}>
                                <button
                                    onClick={() => handlePageChange(page - 1)}
                                    disabled={page === 0}
                                >
                                    Previous
                                </button>
                                <span>Page {page + 1} of {ticketTypes.totalPages}</span>
                                <button
                                    onClick={() => handlePageChange(page + 1)}
                                    disabled={page === ticketTypes.totalPages - 1}
                                >
                                    Next
                                </button>
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