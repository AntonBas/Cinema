import React from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { Layout } from '@/components/layout/Layout/Layout';
import styles from './MoviesLayout.module.css';

const navItems = [
    { path: '/movies/current', icon: '🎬', label: 'Now Playing' },
    { path: '/movies/upcoming', icon: '📅', label: 'Coming Soon' }
];

export const MoviesLayout: React.FC = () => {
    return (
        <Layout>
            <div className={styles.layout}>
                <div className={styles.navContainer}>
                    <div className={styles.header}>
                        <nav className={styles.nav}>
                            <div className={styles.navInner}>
                                {navItems.map(({ path, icon, label }) => (
                                    <NavLink
                                        key={path}
                                        to={path}
                                        className={({ isActive }) =>
                                            `${styles.navLink} ${isActive ? styles.active : ''}`
                                        }
                                    >
                                        <span className={styles.navIcon}>{icon}</span>
                                        <span className={styles.navText}>{label}</span>
                                    </NavLink>
                                ))}
                            </div>
                        </nav>
                    </div>
                </div>

                <main className={styles.content}>
                    <Outlet />
                </main>
            </div>
        </Layout>
    );
};