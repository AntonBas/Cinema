import React from 'react';
import { useAuth } from '@/context/AuthContext';
import styles from './AdminHeader.module.css';

interface AdminHeaderProps {
    onToggleSidebar: () => void;
    isSidebarOpen: boolean;
}

export const AdminHeader: React.FC<AdminHeaderProps> = ({
    onToggleSidebar,
    isSidebarOpen
}) => {
    const { user } = useAuth();

    const fullName = user
        ? `${user.firstName} ${user.lastName}`
        : 'Admin User';

    return (
        <header className={styles.header}>
            <div className={styles.leftSection}>
                <button
                    className={styles.menuButton}
                    onClick={onToggleSidebar}
                    aria-label="Toggle sidebar"
                    title={isSidebarOpen ? "Close sidebar" : "Open sidebar"}
                >
                    <div className={styles.hamburgerIcon}>
                        <span className={isSidebarOpen ? styles.line1Open : styles.line1Closed}></span>
                        <span className={isSidebarOpen ? styles.line2Open : styles.line2Closed}></span>
                        <span className={isSidebarOpen ? styles.line3Open : styles.line3Closed}></span>
                    </div>
                </button>

                <div className={styles.breadcrumb}>
                    <span className={styles.breadcrumbText}>Admin Panel</span>
                </div>
            </div>

            <div className={styles.rightSection}>
                <div className={styles.userInfo}>
                    <div className={styles.userDetails}>
                        <span className={styles.userName}>{fullName}</span>
                        <span className={styles.userRole}>Administrator</span>
                    </div>
                    <div className={styles.statusIndicator}></div>
                </div>
            </div>
        </header>
    );
};