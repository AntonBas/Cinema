import { useState, useEffect, useCallback, useRef } from 'react';
import { useTicketType } from '@/hooks/features/ticketType/useTicketType';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { usePagination } from '@/hooks/common/usePagination';
import { Button } from '@/components/ui/Button/Button';
import { Pagination } from '@/components/ui/Pagination/Pagination';
import { TicketTypeTable } from './TicketTypeTable/TicketTypeTable';
import { TicketTypeFilters } from './TicketTypeFilters/TicketTypeFilters';
import { TicketTypeFormModal } from './TicketTypeFormModal/TicketTypeFormModal';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner/LoadingSpinner';
import { DEFAULT_PAGE_SIZE_COMPACT } from '@/utils/paginationUtils';
import { PageHeader } from '@/components/ui/PageHeader/PageHeader';
import { EmptyState } from '@/components/ui/EmptyState/EmptyState';
import styles from './SectionTicketType.module.css';
import type { TicketTypeResponse, TicketTypeCategory } from '@/types/ticketType';

export const SectionTicketType = () => {
    const [searchQuery, setSearchQuery] = useState('');
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [editingTicketType, setEditingTicketType] = useState<TicketTypeResponse | null>(null);
    const [statusFilter, setStatusFilter] = useState<'all' | 'active' | 'inactive'>('all');
    const [categoryFilter, setCategoryFilter] = useState<TicketTypeCategory | 'all'>('all');

    const { params, setPage } = usePagination({ size: DEFAULT_PAGE_SIZE_COMPACT });
    const { ticketTypes: ticketTypesData, pagination, loading, getAll, remove, toggleActive } = useTicketType();
    const showDelayedLoading = useDelayedLoading(loading, { delay: 150, minDisplayTime: 300 });

    const currentPage = params.page ?? 0;
    const pageSize = params.size ?? 10;

    const loadTicketTypes = useCallback((page: number = currentPage) => {
        getAll({
            page: page,
            size: pageSize,
            query: searchQuery || undefined,
            active: statusFilter === 'all' ? undefined : statusFilter === 'active',
            category: categoryFilter === 'all' ? undefined : categoryFilter
        });
    }, [pageSize, searchQuery, statusFilter, categoryFilter, getAll, currentPage]);

    const didLoadRef = useRef(false);
    useEffect(() => {
        if (didLoadRef.current) return;
        didLoadRef.current = true;
        loadTicketTypes(0);
    }, [loadTicketTypes]);

    const handleSearch = useCallback((query: string) => {
        setSearchQuery(query);
        setPage(0);
        getAll({
            page: 0,
            size: pageSize,
            query: query || undefined,
            active: statusFilter === 'all' ? undefined : statusFilter === 'active',
            category: categoryFilter === 'all' ? undefined : categoryFilter
        });
    }, [pageSize, statusFilter, categoryFilter, getAll, setPage]);

    const handleStatusChange = useCallback((status: 'all' | 'active' | 'inactive') => {
        setStatusFilter(status);
        setPage(0);
        getAll({
            page: 0,
            size: pageSize,
            query: searchQuery || undefined,
            active: status === 'all' ? undefined : status === 'active',
            category: categoryFilter === 'all' ? undefined : categoryFilter
        });
    }, [pageSize, searchQuery, categoryFilter, getAll, setPage]);

    const handleCategoryChange = useCallback((category: TicketTypeCategory | 'all') => {
        setCategoryFilter(category);
        setPage(0);
        getAll({
            page: 0,
            size: pageSize,
            query: searchQuery || undefined,
            active: statusFilter === 'all' ? undefined : statusFilter === 'active',
            category: category === 'all' ? undefined : category
        });
    }, [pageSize, searchQuery, statusFilter, getAll, setPage]);

    const handlePageChange = useCallback((page: number) => {
        setPage(page);
        getAll({
            page: page,
            size: pageSize,
            query: searchQuery || undefined,
            active: statusFilter === 'all' ? undefined : statusFilter === 'active',
            category: categoryFilter === 'all' ? undefined : categoryFilter
        });
    }, [pageSize, searchQuery, statusFilter, categoryFilter, getAll, setPage]);

    const handleCreateSuccess = useCallback(() => {
        setShowCreateModal(false);
        loadTicketTypes(currentPage);
    }, [currentPage, loadTicketTypes]);

    const handleEditSuccess = useCallback(() => {
        setEditingTicketType(null);
        loadTicketTypes(currentPage);
    }, [currentPage, loadTicketTypes]);

    const handleDelete = useCallback(async (id: number) => {
        await remove(id);
        if (ticketTypesData.length === 1 && currentPage > 0) {
            setPage(currentPage - 1);
            getAll({
                page: currentPage - 1,
                size: pageSize,
                query: searchQuery || undefined,
                active: statusFilter === 'all' ? undefined : statusFilter === 'active',
                category: categoryFilter === 'all' ? undefined : categoryFilter
            });
        } else {
            loadTicketTypes(currentPage);
        }
    }, [ticketTypesData.length, currentPage, pageSize, searchQuery, statusFilter, categoryFilter, getAll, setPage, loadTicketTypes, remove]);

    const handleToggleActive = useCallback(async (id: number) => {
        await toggleActive(id);
        loadTicketTypes(currentPage);
    }, [currentPage, loadTicketTypes, toggleActive]);

    if (showDelayedLoading && !ticketTypesData.length) {
        return (
            <div className={styles.loading}>
                <LoadingSpinner text="Loading ticket types..." />
            </div>
        );
    }

    return (
        <div className={styles.section}>
            <PageHeader
                title="Ticket Types"
                subtitle="Configure ticket pricing and categories"
                actions={
                    <Button variant="primary" onClick={() => setShowCreateModal(true)}>
                        Create Ticket Type
                    </Button>
                }
            />

            <div className={styles.content}>
                <TicketTypeFilters
                    onSearchChange={handleSearch}
                    statusFilter={statusFilter}
                    onStatusChange={handleStatusChange}
                    categoryFilter={categoryFilter}
                    onCategoryChange={handleCategoryChange}
                />

                {pagination && pagination.totalElements > 0 && (
                    <div className={styles.resultsInfo}>
                        Showing {pagination.number * pagination.size + 1}-
                        {Math.min((pagination.number + 1) * pagination.size, pagination.totalElements)} of{' '}
                        {pagination.totalElements} ticket types
                        {searchQuery && ` for "${searchQuery}"`}
                    </div>
                )}

                {ticketTypesData.length === 0 ? (
                    <EmptyState title="No Ticket Types Found" />
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
                                    onPageChange={handlePageChange}
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
