import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import styles from './AccountSidebar.module.css';

interface SidebarItem {
    id: string;
    label: string;
    path: string;
    icon: string;
}

interface AccountSidebarProps {
    activePage: string;
}

export const AccountSidebar: React.FC<AccountSidebarProps> = ({ activePage }) => {
    const location = useLocation();

    const sidebarItems: SidebarItem[] = [
        {
            id: 'dashboard',
            label: 'Overview',
            path: '/account',
            icon: '🏠'
        },

        {
            id: 'tickets',
            label: 'My Tickets',
            path: '/account/tickets',
            icon: '🎫'
        },
        {
            id: 'bonuses',
            label: 'Bonuses',
            path: '/account/bonuses',
            icon: '🎁'
        },
        {
            id: 'security',
            label: 'Security',
            path: '/account/security',
            icon: '🔒'
        }
    ];

    const isActive = (path: string) => {
        return location.pathname === path;
    };

    return (
        <aside className={styles.sidebar}>
            <div className={styles.sidebarHeader}>
                <h2 className={styles.sidebarTitle}>My Account</h2>
            </div>

            <nav className={styles.sidebarNav}>
                <ul className={styles.sidebarList}>
                    {sidebarItems.map((item) => (
                        <li key={item.id} className={styles.sidebarItem}>
                            <Link
                                to={item.path}
                                className={`${styles.sidebarLink} ${isActive(item.path) ? styles.active : ''
                                    }`}
                            >
                                <span className={styles.sidebarIcon}>{item.icon}</span>
                                <span className={styles.sidebarLabel}>{item.label}</span>
                            </Link>
                        </li>
                    ))}
                </ul>
            </nav>

            <div className={styles.sidebarFooter}>
                <div className={styles.userSupport}>
                    <p>Need help?</p>
                    <Link to="/support" className={styles.supportLink}>
                        Contact Support
                    </Link>
                </div>
            </div>
        </aside>
    );
};