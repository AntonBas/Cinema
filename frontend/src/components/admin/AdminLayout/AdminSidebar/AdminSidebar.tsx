import React, { useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import styles from './AdminSidebar.module.css';
import clsx from 'clsx';

interface AdminSidebarProps {
    isOpen: boolean;
    isMobile: boolean;
    onClose: () => void;
}

export const AdminSidebar: React.FC<AdminSidebarProps> = ({
    isOpen,
    isMobile,
    onClose
}) => {
    const location = useLocation();
    const navigate = useNavigate();

    const menuItems = [
        { path: '/admin/dashboard', label: 'Dashboard', icon: '📊' },
        { path: '/admin/movies', label: 'Movies', icon: '🎬' },
        { path: '/admin/schedule', label: 'Schedule', icon: '⏰' },
        { path: '/admin/halls', label: 'Halls', icon: '🎭' },
        { path: '/admin/users', label: 'Users', icon: '👥' },
        { path: '/admin/bonus', label: 'Bonus', icon: '🎁' },
        { path: '/admin/promotion', label: 'Promotion', icon: '📢' },
        { path: '/admin/ticket-type', label: 'Ticket Types', icon: '🎫' },
        { path: '/admin/audit-logs', label: 'Audit Logs', icon: '📋' }
    ];

    const handleBackToWebsite = () => {
        navigate('/');
    };

    useEffect(() => {
        if (isMobile) {
            onClose();
        }
    }, [location.pathname, isMobile, onClose]);

    useEffect(() => {
        if (isOpen && isMobile) {
            document.body.style.overflow = 'hidden';
        } else {
            document.body.style.overflow = 'auto';
        }

        return () => {
            document.body.style.overflow = 'auto';
        };
    }, [isOpen, isMobile]);

    return (
        <>
            {isOpen && isMobile && (
                <div className={styles.overlay} onClick={onClose} />
            )}

            <aside className={clsx(
                styles.sidebar,
                isOpen && styles.open,
                !isOpen && !isMobile && styles.closedDesktop,
                isMobile && styles.mobile
            )}>
                <div className={styles.sidebarHeader}>
                    <div className={styles.logoSection}>
                        <div className={styles.logoIcon}>
                            🎬
                        </div>
                        <div className={styles.logoText}>
                            <h2 className={styles.logoTitle}>Cinema</h2>
                            <p className={styles.logoSubtitle}>Admin Panel</p>
                        </div>
                    </div>
                </div>

                <nav className={styles.nav}>
                    {menuItems.map(item => (
                        <Link
                            key={item.path}
                            to={item.path}
                            className={clsx(
                                styles.item,
                                location.pathname === item.path && styles.active
                            )}
                            onClick={onClose}
                        >
                            <span className={styles.icon}>{item.icon}</span>
                            <span className={styles.label}>{item.label}</span>
                            {location.pathname === item.path && (
                                <div className={styles.activeIndicator}></div>
                            )}
                        </Link>
                    ))}
                </nav>

                <div className={styles.sidebarFooter}>
                    <button
                        className={styles.backButton}
                        onClick={handleBackToWebsite}
                    >
                        <span className={styles.backIcon}>←</span>
                        <span className={styles.backText}>Back to Website</span>
                    </button>
                </div>
            </aside>
        </>
    );
};