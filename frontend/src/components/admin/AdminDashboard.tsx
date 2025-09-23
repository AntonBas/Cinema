import React from 'react';
import './AdminDashboard.css';

export const AdminDashboard: React.FC = () => {
  return (
    <div className="admin-dashboard">
      <h1>Admin Dashboard</h1>
      <div className="dashboard-stats">
        <div className="stat-card">
          <h3>🎬 Movies</h3>
          <p className="stat-number">24</p>
          <span className="stat-label">Total</span>
        </div>
        <div className="stat-card">
          <h3>⏰ Sessions</h3>
          <p className="stat-number">56</p>
          <span className="stat-label">Today</span>
        </div>
        <div className="stat-card">
          <h3>🎭 Halls</h3>
          <p className="stat-number">5</p>
          <span className="stat-label">Available</span>
        </div>
        <div className="stat-card">
          <h3>👥 Users</h3>
          <p className="stat-number">1,234</p>
          <span className="stat-label">Registered</span>
        </div>
      </div>
      
      <div className="recent-activity">
        <h2>Recent Activity</h2>
        <div className="activity-list">
          <div className="activity-item">
            <span className="activity-icon">🎬</span>
            <div className="activity-content">
              <p>New movie "Avatar 2" added</p>
              <span className="activity-time">2 hours ago</span>
            </div>
          </div>
          <div className="activity-item">
            <span className="activity-icon">⏰</span>
            <div className="activity-content">
              <p>Schedule updated for Hall 3</p>
              <span className="activity-time">5 hours ago</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};