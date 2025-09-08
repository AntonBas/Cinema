import React, { useState, useEffect, useRef } from 'react';
import { useAuth } from '../../context/AuthContext';
import './Header.css';

export const Header: React.FC = () => {
  const { user, token, logout } = useAuth();
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const dropdownRef = useRef<HTMLLIElement>(null);
  const mobileMenuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsDropdownOpen(false);
      }
      if (mobileMenuRef.current &&
        !mobileMenuRef.current.contains(event.target as Node) &&
        !(event.target as Element).closest('.mobile-menu-btn')) {
        setIsMobileMenuOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const handleLogout = () => {
    logout();
    setIsDropdownOpen(false);
    setIsMobileMenuOpen(false);
    window.location.href = '/';
  };

  const toggleDropdown = () => {
    setIsDropdownOpen(!isDropdownOpen);
  };

  const toggleMobileMenu = () => {
    setIsMobileMenuOpen(!isMobileMenuOpen);
  };

  return (
    <header className="header-main">
      <nav className="header-navbar">
        <div className="logo">
          <a href="/">Cinema</a>
        </div>

        <ul className="nav-links">
          <li><a href="/" onClick={() => setIsDropdownOpen(false)}>Home</a></li>
          <li><a href="/movies" onClick={() => setIsDropdownOpen(false)}>Movies</a></li>
          <li><a href="/schedule" onClick={() => setIsDropdownOpen(false)}>Schedule</a></li>

          {token ? (
            <li className="dropdown" ref={dropdownRef}>
              <button className="dropdown-btn" onClick={toggleDropdown}>
                My Account
                <span className={`dropdown-arrow ${isDropdownOpen ? 'open' : ''}`}>▼</span>
              </button>
              {isDropdownOpen && (
                <ul className="dropdown-menu">
                  <li><a href="/account" onClick={() => setIsDropdownOpen(false)}>Profile</a></li>
                  {user?.userRole === 'ROLE_ADMIN' && (
                    <li><a href="/admin/dashboard" onClick={() => setIsDropdownOpen(false)}>Dashboard</a></li>
                  )}
                  <li>
                    <button onClick={handleLogout} className="logout-btn">
                      Logout
                    </button>
                  </li>
                </ul>
              )}
            </li>
          ) : (
            <li><a href="/login">Login/Register</a></li>
          )}
        </ul>

        <button className="mobile-menu-btn" onClick={toggleMobileMenu}>
          <span></span>
          <span></span>
          <span></span>
        </button>

        <div ref={mobileMenuRef} className={`mobile-menu ${isMobileMenuOpen ? 'open' : ''}`}>
          <div className="mobile-menu-content">
            <a href="/" onClick={() => setIsMobileMenuOpen(false)}>Home</a>
            <a href="/movies" onClick={() => setIsMobileMenuOpen(false)}>Movies</a>
            <a href="/schedule" onClick={() => setIsMobileMenuOpen(false)}>Schedule</a>

            {token ? (
              <div className="mobile-account-section">
                <a href="/account" onClick={() => setIsMobileMenuOpen(false)}>Profile</a>
                {user?.userRole === 'ROLE_ADMIN' && (
                  <a href="/admin/dashboard" onClick={() => setIsMobileMenuOpen(false)}>Dashboard</a>
                )}
                <button onClick={handleLogout} className="mobile-logout-btn">
                  Logout
                </button>
              </div>
            ) : (
              <a href="/login" onClick={() => setIsMobileMenuOpen(false)}>Login/Register</a>
            )}
          </div>
        </div>
      </nav>
    </header>
  );
};