import React from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';
import { Layout } from '@/components/layout/Layout';
import styles from './MoviesLayout.module.css';

export const MoviesLayout: React.FC = () => {
    const location = useLocation();

    return (
        <Layout>
            <nav className={styles.nav}>
                <Link
                    to="/movies/current"
                    className={`${styles.navLink} ${location.pathname === '/movies/current' ? styles.active : ''}`}
                >
                    Now Playing
                </Link>
                <Link
                    to="/movies/upcoming"
                    className={`${styles.navLink} ${location.pathname === '/movies/upcoming' ? styles.active : ''}`}
                >
                    Coming Soon
                </Link>
            </nav>

            <div className={styles.content}>
                <Outlet />
            </div>
        </Layout>
    );
};