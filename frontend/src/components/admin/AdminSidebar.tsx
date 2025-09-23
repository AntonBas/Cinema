import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import './AdminSidebar.css';

export const AdminSidebar: React.FC = () => {
  const location = useLocation();
  
  const menuItems = [
    { path: '/admin/dashboard', label: 'Dashboard', icon: '📊' },
    { path: '/admin/movies', label: 'Movies', icon: '🎬' },
    { path: '/admin/halls', label: 'Halls', icon: '🎭' },
    { path: '/admin/schedule', label: 'Schedule', icon: '⏰' }
  ];

  return (
    <aside className="admin-sidebar">
      <div className="sidebar-header">
        <h2>Admin Panel</h2>
      </div>
      <nav className="sidebar-nav">
        {menuItems.map(item => (
          <Link
            key={item.path}
            to={item.path}
            className={`nav-item ${location.pathname === item.path ? 'active' : ''}`}
          >
            <span className="nav-icon">{item.icon}</span>
            <span className="nav-label">{item.label}</span>
          </Link>
        ))}
      </nav>
    </aside>
  );
};