// src/components/layout/Header.tsx
import React, { useState, useEffect, useRef } from 'react';
import { useAuth } from '../../context/AuthContext';
import './Header.css';

export const Header: React.FC = () => {
  const { user, token, logout } = useAuth();
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const dropdownRef = useRef<HTMLLIElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsDropdownOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const handleLogout = () => {
    logout();
    window.location.href = '/';
  };

  const toggleDropdown = () => {
    setIsDropdownOpen(!isDropdownOpen);
  };

  return (
    <header className="header-main">
      <nav className="header-navbar">
        <div className="logo">
          <a href="/">Cinema</a>
        </div>
        
        <ul className="nav-links">
          <li><a href="/">Home</a></li>
          <li><a href="/movies">Movies</a></li>
          <li><a href="/schedule">Schedule</a></li>
          
          {token ? (
            <li className="dropdown" ref={dropdownRef}>
              <button className="dropdown-btn" onClick={toggleDropdown}>
                My Account
              </button>
              {isDropdownOpen && (
                <ul className="dropdown-menu">
                  <li><a href="/account">Profile</a></li>
                  {user?.userRole === 'ROLE_ADMIN' && (
                    <li><a href="/admin/dashboard">Dashboard</a></li>
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
      </nav>
    </header>
  );
};