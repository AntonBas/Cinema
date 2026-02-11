import React from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';
import { Layout } from '@/components/layout/Layout/Layout';
import styles from './MoviesLayout.module.css';

export const MoviesLayout: React.FC = () => {
    const location = useLocation();

    return (
        <Layout>
            <div className={styles.layout}>
                <div className={styles.navContainer}>
                    <div className={styles.header}>
                        <nav className={styles.nav}>
                            <div className={styles.navInner}>
                                <Link
                                    to="/movies/current"
                                    className={`${styles.navLink} ${location.pathname === '/movies/current' ? styles.active : ''}`}
                                >
                                    <span className={styles.navIcon}>🎬</span>
                                    <span className={styles.navText}>Now Playing</span>
                                </Link>
                                <Link
                                    to="/movies/upcoming"
                                    className={`${styles.navLink} ${location.pathname === '/movies/upcoming' ? styles.active : ''}`}
                                >
                                    <span className={styles.navIcon}>📅</span>
                                    <span className={styles.navText}>Coming Soon</span>
                                </Link>
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