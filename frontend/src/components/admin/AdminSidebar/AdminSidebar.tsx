import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import styles from './AdminSidebar.module.css';
import clsx from 'clsx';

interface AdminSidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

export const AdminSidebar: React.FC<AdminSidebarProps> = ({ isOpen, onClose }) => {
  const location = useLocation();

  const menuItems = [
    { path: '/admin/dashboard', label: 'Dashboard', icon: '📊' },
    { path: '/admin/movies', label: 'Movies', icon: '🎬' },
    { path: '/admin/halls', label: 'Halls', icon: '🎭' },
    { path: '/admin/schedule', label: 'Schedule', icon: '⏰' }
  ];

  return (
    <>
      <div
        className={clsx(styles.overlay, isOpen && styles.active)}
        onClick={onClose}
      />

      <aside className={clsx(styles.sidebar, isOpen && styles.open)}>
        <div className={styles.header}>
          <h2>Admin Panel</h2>
          <button className={styles.close} onClick={onClose}>×</button>
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
            </Link>
          ))}
        </nav>
      </aside>
    </>
  );
};