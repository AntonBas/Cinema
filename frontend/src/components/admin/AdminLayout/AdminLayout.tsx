import React, { useState, useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import { AdminSidebar } from './AdminSidebar/AdminSidebar';
import { AdminHeader } from './AdminHeader/AdminHeader';
import styles from './AdminLayout.module.css';

export const AdminLayout: React.FC = () => {
    const [isSidebarOpen, setIsSidebarOpen] = useState(true);
    const [isMobile, setIsMobile] = useState(false);

    useEffect(() => {
        const checkMobile = () => {
            const mobile = window.innerWidth <= 768;
            setIsMobile(mobile);
            if (mobile) {
                setIsSidebarOpen(false);
            } else {
                setIsSidebarOpen(true);
            }
        };

        checkMobile();
        window.addEventListener('resize', checkMobile);

        return () => {
            window.removeEventListener('resize', checkMobile);
        };
    }, []);

    const toggleSidebar = () => {
        setIsSidebarOpen(!isSidebarOpen);
    };

    const handleCloseSidebar = () => {
        if (isMobile) {
            setIsSidebarOpen(false);
        }
    };

    return (
        <div className={styles.adminLayout}>
            <AdminSidebar
                isOpen={isSidebarOpen}
                isMobile={isMobile}
                onClose={handleCloseSidebar}
            />

            <div className={`${styles.mainContent} ${!isSidebarOpen && styles.fullWidth}`}>
                <AdminHeader
                    onToggleSidebar={toggleSidebar}
                    isSidebarOpen={isSidebarOpen}
                />

                <main className={styles.content}>
                    <Outlet />
                </main>

                <footer className={styles.adminFooter}>
                    <div className={styles.footerContent}>
                        <div>© {new Date().getFullYear()} Cinema Admin Panel</div>
                    </div>
                </footer>
            </div>
        </div>
    );
};