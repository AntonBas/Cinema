import React from 'react';
import { Link, Outlet, useNavigate } from 'react-router-dom';
import './Layout.css';

const Layout: React.FC = () => {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || 'null');

  const handleLogout = () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('user');
    navigate('/login');
  };

  return (
    <div className="layout">
      <header className="header">
        <nav className="nav">
          <Link to="/" className="logo">МійДодаток</Link>
          
          <div className="nav-links">
            <Link to="/">Головна</Link>
            {user && (
              <>
                <Link to="/admin/movie">Фільми</Link>
                <Link to="/admin/person">Персони</Link>
                <button onClick={handleLogout} className="logout-btn">
                  Вийти
                </button>
              </>
            )}
            {!user && (
              <Link to="/login">Увійти</Link>
            )}
          </div>
        </nav>
      </header>

      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
};

export default Layout;