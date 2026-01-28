import React, { useState, useEffect } from 'react';
import { Layout } from '@/components/layout/Layout/Layout';
import { AccountSidebar } from '@/components/account/AccountSidebar/AccountSidebar';
import { TicketFilters } from '@/components/account/TicketFilters/TicketFilters';
import { TicketsList } from '@/components/account/TicketsList/TicketsList';
import { TicketQRModal } from '@/components/account/TicketQRModal/TicketQRModal';
import { TicketRefundModal } from '@/components/account/TicketRefundModal/TicketRefundModal';
import { Button, Input, Notification } from '@/components/ui';
import { useTickets } from '@/hooks/features/tickets/useTickets';
import { useTicketFilters } from '@/hooks/features/tickets/useTicketFilters';
import type { TicketResponse, TicketStatus } from '@/types/ticket';
import { Grid, List, RefreshCw, Search } from 'lucide-react';
import styles from './TicketsPage.module.css';

export const TicketsPage: React.FC = () => {
    const { getUserTickets, loading } = useTickets();
    const [tickets, setTickets] = useState<TicketResponse[]>([]);
    const [activeTab, setActiveTab] = useState<TicketStatus | 'all'>('all');
    const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
    const [searchQuery, setSearchQuery] = useState('');
    const [showQRModal, setShowQRModal] = useState(false);
    const [showRefundModal, setShowRefundModal] = useState(false);
    const [selectedQRCode, setSelectedQRCode] = useState<string>('');
    const [selectedTicket, setSelectedTicket] = useState<TicketResponse | null>(null);
    const [notification, setNotification] = useState<{ type: 'success' | 'error', message: string } | null>(null);

    const {
        filteredTickets,
        updateStatusFilter,
        updateSearchQuery,
        updateDateRange,
        clearFilters,
        statistics,
        hasFilters
    } = useTicketFilters(tickets);

    useEffect(() => {
        loadTickets();
    }, []);

    useEffect(() => {
        updateStatusFilter(activeTab === 'all' ? undefined : activeTab);
    }, [activeTab, updateStatusFilter]);

    useEffect(() => {
        updateSearchQuery(searchQuery);
    }, [searchQuery, updateSearchQuery]);

    const loadTickets = async () => {
        try {
            const data = await getUserTickets();
            setTickets(data);
            showNotification('success', 'Tickets updated successfully');
        } catch (error) {
            console.error('Failed to load tickets:', error);
            showNotification('error', 'Failed to load tickets');
        }
    };

    const handleShowQR = (ticketCode: string) => {
        setSelectedQRCode(ticketCode);
        setShowQRModal(true);
    };

    const handleRequestRefund = (ticket: TicketResponse) => {
        setSelectedTicket(ticket);
        setShowRefundModal(true);
    };

    const handleRefundSuccess = () => {
        showNotification('success', 'Refund request submitted successfully');
        loadTickets();
    };

    const showNotification = (type: 'success' | 'error', message: string) => {
        setNotification({ type, message });
        setTimeout(() => setNotification(null), 3000);
    };

    const tabs: Array<{ id: TicketStatus | 'all', label: string, count: number }> = [
        { id: 'all', label: 'All', count: statistics.total },
        { id: 'ACTIVE', label: 'Active', count: statistics.activeCount },
        { id: 'USED', label: 'Used', count: statistics.usedCount },
        { id: 'CANCELLED', label: 'Cancelled', count: statistics.cancelledCount }
    ];

    const statusTabs: Array<{ id: TicketStatus, label: string, count: number }> = [
        { id: 'REFUNDED', label: 'Refunded', count: statistics.byStatus?.REFUNDED || 0 },
        { id: 'PENDING', label: 'Pending', count: statistics.byStatus?.PENDING || 0 },
        { id: 'EXPIRED', label: 'Expired', count: statistics.byStatus?.EXPIRED || 0 }
    ];

    return (
        <Layout>
            <div className={styles.ticketsPage}>
                <div className={styles.container}>
                    <AccountSidebar activePage="tickets" />

                    <main className={styles.mainContent}>
                        {notification && (
                            <Notification
                                id="tickets-notification"
                                message={notification.message}
                                type={notification.type}
                                isVisible={true}
                                onClose={() => setNotification(null)}
                                duration={3000}
                                position={0}
                            />
                        )}

                        <div className={styles.pageHeader}>
                            <div>
                                <h1 className={styles.pageTitle}>My Tickets</h1>
                                <p className={styles.pageSubtitle}>Manage and view your movie tickets</p>
                            </div>
                            <div className={styles.refreshButton}>
                                <RefreshCw size={18} />
                                <Button
                                    variant="primary"
                                    onClick={loadTickets}
                                    loading={loading}
                                    disabled={loading}
                                >
                                    Refresh
                                </Button>
                            </div>
                        </div>

                        <div className={styles.statsCards}>
                            <div className={styles.statCard}>
                                <div className={styles.statValue}>{statistics.total}</div>
                                <div className={styles.statLabel}>Total Tickets</div>
                            </div>
                            <div className={styles.statCard}>
                                <div className={styles.statValue}>{statistics.activeCount}</div>
                                <div className={styles.statLabel}>Active</div>
                            </div>
                            <div className={styles.statCard}>
                                <div className={styles.statValue}>{statistics.usedCount}</div>
                                <div className={styles.statLabel}>Used</div>
                            </div>
                            <div className={styles.statCard}>
                                <div className={`${styles.statValue} ${styles.revenue}`}>
                                    {statistics.totalRevenue.toFixed(2)} UAH
                                </div>
                                <div className={styles.statLabel}>Total Spent</div>
                            </div>
                        </div>

                        <div className={styles.controlsSection}>
                            <div className={styles.searchBox}>
                                <Search size={20} className={styles.searchIcon} />
                                <Input
                                    type="text"
                                    placeholder="Search tickets by movie, hall, or code..."
                                    value={searchQuery}
                                    onChange={setSearchQuery}
                                    className={styles.searchInput}
                                />
                                {hasFilters && (
                                    <Button
                                        variant="cancel"
                                        onClick={clearFilters}
                                        size="small"
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
                                    >
                                        <Grid size={18} /> Grid
                                    </Button>
                                    <Button
                                        variant={viewMode === 'list' ? 'primary' : 'secondary'}
                                        onClick={() => setViewMode('list')}
                                        size="small"
                                    >
                                        <List size={18} /> List
                                    </Button>
                                </div>
                            </div>
                        </div>

                        <div className={styles.filtersSection}>
                            <TicketFilters
                                onSearch={updateSearchQuery}
                                onDateRangeChange={updateDateRange}
                                onClearFilters={clearFilters}
                                hasFilters={hasFilters}
                            />
                        </div>

                        <div className={styles.tabsSection}>
                            <div className={styles.mainTabs}>
                                {tabs.map(tab => (
                                    <button
                                        key={tab.id}
                                        className={`${styles.tab} ${activeTab === tab.id ? styles.active : ''}`}
                                        onClick={() => setActiveTab(tab.id)}
                                    >
                                        <span className={styles.tabLabel}>{tab.label}</span>
                                        <span className={styles.tabCount}>{tab.count}</span>
                                    </button>
                                ))}
                            </div>
                            <div className={styles.statusTabs}>
                                {statusTabs.map(tab => (
                                    <button
                                        key={tab.id}
                                        className={`${styles.statusTab} ${activeTab === tab.id ? styles.active : ''}`}
                                        onClick={() => setActiveTab(tab.id)}
                                    >
                                        {tab.label}
                                        <span className={styles.statusTabCount}>{tab.count}</span>
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
                                <TicketsList
                                    tickets={filteredTickets}
                                    viewMode={viewMode}
                                    onShowQR={handleShowQR}
                                    onRequestRefund={handleRequestRefund}
                                />
                            )}
                        </div>
                    </main>
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