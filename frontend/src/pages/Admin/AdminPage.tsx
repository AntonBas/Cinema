import React, { useState } from 'react';
import { Routes, Route } from 'react-router-dom';
import { Header } from '@/components/layout/Header/Header';
import { Footer } from '@/components/layout/Footer/Footer';
import { AdminSidebar } from '@/components/admin/AdminSidebar/AdminSidebar';
import { AdminDashboard } from '@/components/admin/AdminDashboard/AdminDashboard';
import { SectionMovies } from '@/components/admin/SectionMovies/SectionMovies';
import { SectionHalls } from '@/components/admin/SectionHalls/SectionHalls';
import { SectionSchedule } from '@/components/admin/SectionSchedule';
import { SectionUsers } from '@/components/admin/SectionUsers/SectionUsers';
import './AdminPage.css';

export const AdminPage: React.FC = () => {
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

  return (
    <div className="admin-layout">
      <Header />

      <button
        className="mobile-menu-toggle"
        onClick={() => setIsSidebarOpen(true)}
      >
        ☰
      </button>

      <div className="admin-container">
        <AdminSidebar
          isOpen={isSidebarOpen}
          onClose={() => setIsSidebarOpen(false)}
        />

        <main className="admin-content">
          <Routes>
            <Route index element={<AdminDashboard />} />
            <Route path="dashboard" element={<AdminDashboard />} />
            <Route path="movies" element={<SectionMovies />} />
            <Route path="halls" element={<SectionHalls />} />
            <Route path="schedule" element={<SectionSchedule />} />
            <Route path="users" element={<SectionUsers />} />
          </Routes>
        </main>
      </div>

      <Footer />
    </div>
  );
};