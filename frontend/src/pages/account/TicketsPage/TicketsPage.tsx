import React, { useState, useEffect, useMemo } from 'react';
import { Layout } from '@/components/layout/Layout/Layout';
import { AccountSidebar } from '@/components/account/AccountSidebar/AccountSidebar';
import { TicketsList } from '@/components/account/TicketSection/TicketsList/TicketsList';
import { TicketQRModal } from '@/components/account/TicketSection/TicketQRModal/TicketQRModal';
import { TicketRefundModal } from '@/components/account/TicketSection/TicketRefundModal/TicketRefundModal';
import { Pagination } from '@/components/ui/Pagination/Pagination';
import { Button } from '@/components/ui/Button/Button';
import { Notification } from '@/components/ui/Notification/Notification';
import { SearchInput } from '@/components/ui/SearchInput/SearchInput';
import { useTickets } from '@/hooks/features/tickets/useTickets';
import type { TicketStatus } from '@/types/ticket';
import { Grid, List } from 'lucide-react';
import styles from './TicketsPage.module.css';

export const TicketsPage: React.FC = () => {
    const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
    const [showQRModal, setShowQRModal] = useState(false);
    const [showRefundModal, setShowRefundModal] = useState(false);
    const [selectedQRCode, setSelectedQRCode] = useState<string>('');
    const [selectedTicket, setSelectedTicket] = useState<any>(null);
    const [notification, setNotification] = useState<{ type: 'success' | 'error', message: string } | null>(null);

    const {
        tickets,
        pagination,
        loading,
        getUserTickets
    } = useTickets();

    const [statusFilter, setStatusFilter] = useState<TicketStatus | undefined>(undefined);
    const [searchQuery, setSearchQuery] = useState('');
    const [currentPage, setCurrentPage] = useState(0);

    useEffect(() => {
        loadTickets();
    }, [statusFilter, searchQuery, currentPage]);

    const loadTickets = async () => {
        const params: any = {
            page: currentPage,
            size: 10
        };

        if (statusFilter) {
            params.status = statusFilter;
        }

        if (searchQuery.trim()) {
            params.movieTitle = searchQuery;
        }

        await getUserTickets(params);
    };

    const refreshTickets = async () => {
        await loadTickets();
    };

    const hasActiveFilters = useMemo(() => {
        return searchQuery.trim() !== '' || statusFilter !== undefined;
    }, [searchQuery, statusFilter]);

    const handleShowQR = (ticketCode: string) => {
        setSelectedQRCode(ticketCode);
        setShowQRModal(true);
    };

    const handleRequestRefund = (ticket: any) => {
        setSelectedTicket(ticket);
        setShowRefundModal(true);
    };

    const handleRefundSuccess = () => {
        showNotification('success', 'Refund request submitted successfully');
        refreshTickets();
    };

    const showNotification = (type: 'success' | 'error', message: string) => {
        setNotification({ type, message });
        setTimeout(() => setNotification(null), 3000);
    };

    const handleClearAllFilters = () => {
        setStatusFilter(undefined);
        setSearchQuery('');
        setCurrentPage(0);
    };

    const handleStatusChange = (status: TicketStatus | undefined) => {
        setStatusFilter(status);
        setCurrentPage(0);
    };

    const handleSearch = (query: string) => {
        setSearchQuery(query);
        setCurrentPage(0);
    };

    const handlePageChange = (page: number) => {
        setCurrentPage(page);
    };

    const tabs: Array<{ id: TicketStatus | 'all', label: string }> = [
        { id: 'all', label: 'All' },
        { id: 'ACTIVE', label: 'Active' },
        { id: 'USED', label: 'Used' },
        { id: 'REFUNDED', label: 'Refunded' }
    ];

    return (
        <Layout>
            <div className={styles.ticketsPage}>
                <div className={styles.container}>
                    <AccountSidebar activePage="tickets" />

                    <div className={styles.mainContent}>
                        {notification && (
                            <Notification
                                id="tickets-notification"
                                message={notification.message}
                                type={notification.type}
                                isVisible={true}
                                onClose={() => setNotification(null)}
                                duration={3000}
                            />
                        )}

                        <div className={styles.pageHeader}>
                            <div>
                                <h1 className={styles.pageTitle}>My Tickets</h1>
                                <p className={styles.pageSubtitle}>Manage and view your movie tickets</p>
                            </div>
                        </div>

                        <div className={styles.controlsSection}>
                            <div className={styles.searchBox}>
                                <SearchInput
                                    onSearch={handleSearch}
                                    placeholder="Search tickets by movie title..."
                                    delay={300}
                                    className={styles.searchInput}
                                />
                                {hasActiveFilters && (
                                    <Button
                                        variant="outline"
                                        onClick={handleClearAllFilters}
                                        size="small"
                                        className={styles.clearButton}
                                    >
                                        Clear Filters
                                    </Button>
                                )}
                            </div>

                            <div className={styles.viewControls}>
                                <div className={styles.viewButtons}>
                                    <Button
                                        variant={viewMode === 'grid' ? 'primary' : 'secondary'}
                                        onClick={() => setViewMode('grid')}
                                        size="small"
                                        className={styles.viewButton}
                                    >
                                        <Grid size={18} /> Grid
                                    </Button>
                                    <Button
                                        variant={viewMode === 'list' ? 'primary' : 'secondary'}
                                        onClick={() => setViewMode('list')}
                                        size="small"
                                        className={styles.viewButton}
                                    >
                                        <List size={18} /> List
                                    </Button>
                                </div>
                            </div>
                        </div>

                        <div className={styles.tabsSection}>
                            <div className={styles.filterTabs}>
                                {tabs.map(tab => (
                                    <button
                                        key={tab.id}
                                        className={`${styles.filterTab} ${statusFilter === (tab.id === 'all' ? undefined : tab.id) ? styles.active : ''}`}
                                        onClick={() => handleStatusChange(tab.id === 'all' ? undefined : tab.id)}
                                    >
                                        <span className={styles.tabLabel}>{tab.label}</span>
                                    </button>
                                ))}
                            </div>
                        </div>

                        <div className={styles.ticketsSection}>
                            {loading ? (
                                <div className={styles.loadingState}>
                                    <div className={styles.spinner}></div>
                                    <p>Loading tickets...</p>
                                </div>
                            ) : (
                                <>
                                    <TicketsList
                                        tickets={tickets}
                                        viewMode={viewMode}
                                        onShowQR={handleShowQR}
                                        onRequestRefund={handleRequestRefund}
                                    />
                                    {pagination && pagination.totalPages > 1 && (
                                        <div className={styles.paginationContainer}>
                                            <Pagination
                                                currentPage={pagination.number}
                                                totalPages={pagination.totalPages}
                                                totalElements={pagination.totalElements}
                                                pageSize={pagination.size}
                                                onPageChange={handlePageChange}
                                                variant="pages"
                                                showInfo={true}
                                            />
                                        </div>
                                    )}
                                </>
                            )}
                        </div>
                    </div>
                </div>

                {showQRModal && (
                    <TicketQRModal
                        ticketCode={selectedQRCode}
                        onClose={() => setShowQRModal(false)}
                    />
                )}

                {showRefundModal && (
                    <TicketRefundModal
                        ticket={selectedTicket}
                        onClose={() => {
                            setShowRefundModal(false);
                            setSelectedTicket(null);
                        }}
                        onRefundSuccess={handleRefundSuccess}
                    />
                )}
            </div>
        </Layout>
    );
};