import { useState, useEffect, useCallback, useRef } from 'react';
import { useTicketType } from '@/hooks/features/ticketType/useTicketType';
import { useNotification } from '@/hooks/common/useNotification';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { usePagination } from '@/hooks/common/usePagination';
import { Button } from '@/components/ui/Button/Button';
import { Pagination } from '@/components/ui/Pagination/Pagination';
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

    const { params, setPage } = usePagination({ size: 10 });

    const isInitialMount = useRef(true);

    const {
        ticketTypes,
        loading,
        getAll,
        remove: deleteTicketType,
        toggleActive
    } = useTicketType();

    const { notifications, showNotification, hideNotification } = useNotification();
    const showDelayedLoading = useDelayedLoading(loading, { delay: 150, minDisplayTime: 300 });

    const currentPage = params.page ?? 0;
    const pageSize = params.size ?? 10;

    const loadTicketTypes = useCallback(async (skipCache = true) => {
        try {
            await getAll({
                page: currentPage,
                size: pageSize,
                active: statusFilter === 'all' ? undefined : statusFilter === 'active',
                category: categoryFilter === 'all' ? undefined : categoryFilter,
                search: searchQuery.trim() || undefined
            }, skipCache);
        } catch (error) {
            showNotification('Failed to load ticket types', 'error');
        }
    }, [getAll, currentPage, pageSize, statusFilter, categoryFilter, searchQuery, showNotification]);

    useEffect(() => {
        loadTicketTypes(false);
    }, []);

    useEffect(() => {
        if (isInitialMount.current) {
            isInitialMount.current = false;
            return;
        }

        const timer = setTimeout(() => {
            loadTicketTypes(true);
        }, 100);

        return () => clearTimeout(timer);
    }, [currentPage, statusFilter, categoryFilter, searchQuery]);

    const handlePageChange = useCallback((newPage: number) => {
        setPage(newPage);
    }, [setPage]);

    const handleCreateSuccess = useCallback((newTicketType?: TicketTypeResponse) => {
        setShowCreateModal(false);
        if (newTicketType) {
            showNotification(`Ticket type "${newTicketType.displayName}" created successfully!`, 'success');
        } else {
            showNotification('Ticket type created successfully!', 'success');
        }
        setPage(0);
        loadTicketTypes(true);
    }, [loadTicketTypes, showNotification, setPage]);

    const handleEditSuccess = useCallback((updatedTicketType?: TicketTypeResponse) => {
        setEditingTicketType(null);
        if (updatedTicketType) {
            showNotification(`Ticket type "${updatedTicketType.displayName}" updated successfully!`, 'success');
        } else {
            showNotification('Ticket type updated successfully!', 'success');
        }
        loadTicketTypes(true);
    }, [loadTicketTypes, showNotification]);

    const handleDelete = useCallback(async (id: number, displayName: string) => {
        try {
            await deleteTicketType(id);
            showNotification(`Ticket type "${displayName}" deleted successfully!`, 'success');

            if (ticketTypes?.content.length === 1 && currentPage > 0) {
                setPage(currentPage - 1);
            } else {
                await loadTicketTypes(true);
            }
        } catch (err) {
            showNotification('Failed to delete ticket type', 'error');
        }
    }, [deleteTicketType, loadTicketTypes, showNotification, ticketTypes, currentPage, setPage]);

    const handleToggleActive = useCallback(async (id: number, displayName: string) => {
        try {
            const updated = await toggleActive(id);
            const status = updated?.active ? 'activated' : 'deactivated';
            showNotification(`Ticket type "${displayName}" ${status} successfully!`, 'success');
            await loadTicketTypes(true);
        } catch (err) {
            showNotification('Failed to toggle active status', 'error');
        }
    }, [toggleActive, loadTicketTypes, showNotification]);

    const handleEdit = useCallback((ticketType: TicketTypeResponse) => {
        setEditingTicketType(ticketType);
    }, []);

    const ticketTypeList = ticketTypes?.content || [];

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
                <Button
                    variant="primary"
                    onClick={() => setShowCreateModal(true)}
                >
                    Create Ticket Type
                </Button>
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
                    </div>
                ) : (
                    <>
                        <TicketTypeTable
                            ticketTypes={ticketTypeList}
                            onEdit={handleEdit}
                            onDelete={handleDelete}
                            onToggleActive={handleToggleActive}
                        />

                        {ticketTypes && ticketTypes.totalPages > 1 && (
                            <Pagination
                                currentPage={ticketTypes.number}
                                totalPages={ticketTypes.totalPages}
                                totalElements={ticketTypes.totalElements}
                                pageSize={ticketTypes.size}
                                onPageChange={handlePageChange}
                                variant="pages"
                                showInfo={true}
                            />
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