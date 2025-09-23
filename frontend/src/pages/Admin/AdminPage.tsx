import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { Header } from '../../components/layout/Header';
import { Footer } from '../../components/layout/Footer';
import { AdminSidebar } from '../../components/admin/AdminSidebar';
import { AdminDashboard } from '../../components/admin/AdminDashboard';
import { MoviesSection } from '../../components/admin/MoviesSection';
import './AdminPage.css';

export const AdminPage: React.FC = () => {
  return (
    <>
      <Header />
      <div className="admin-page">
        <AdminSidebar />
        <main className="admin-content">
          <Routes>
            <Route index element={<AdminDashboard />} />
            <Route path="dashboard" element={<AdminDashboard />} />
            <Route path="movies" element={<MoviesSection />} />
          </Routes>
        </main>
      </div>
      <Footer />
    </>
  );
};