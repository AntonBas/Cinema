import React, { useState, useEffect, useRef } from 'react';
import { useAuth } from '../../context/AuthContext';
import './Header.css';
import { Link } from 'react-router-dom';

interface NavLink {
  name: string;
  path: string;
  adminOnly?: boolean;
}

export const Header: React.FC = () => {
  const { user, token, logout } = useAuth();
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const dropdownRef = useRef<HTMLLIElement>(null);
  const mobileMenuRef = useRef<HTMLDivElement>(null);

  const links: NavLink[] = [
    { name: 'Home', path: '/' },
    { name: 'Movies', path: '/movies' },
    { name: 'Schedule', path: '/schedule' },
  ];

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsDropdownOpen(false);
      }
      if (
        mobileMenuRef.current &&
        !mobileMenuRef.current.contains(event.target as Node) &&
        !(event.target as Element).closest('.mobile-menu-btn')
      ) {
        setIsMobileMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = () => {
    logout();
    setIsDropdownOpen(false);
    setIsMobileMenuOpen(false);
    window.location.href = '/';
  };

  return (
    <header className="header-main">
      <nav className="header-navbar">
        <div className="logo">
          <Link to="/">Cinema</Link>
        </div>

        <ul className="nav-links">
          {links.map((link) => (
            <li key={link.name}>
              <Link to={link.path} onClick={() => setIsDropdownOpen(false)}>
                {link.name}
              </Link>
            </li>
          ))}

          {token ? (
            <li className="dropdown" ref={dropdownRef}>
              <button
                className="dropdown-btn"
                onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                aria-haspopup="true"
                aria-expanded={isDropdownOpen}
              >
                My Account <span className={`dropdown-arrow ${isDropdownOpen ? 'open' : ''}`}>▼</span>
              </button>
              {isDropdownOpen && (
                <ul className="dropdown-menu">
                  <li>
                    <Link to="/account" onClick={() => setIsDropdownOpen(false)}>
                      Profile
                    </Link>
                  </li>
                  {user?.userRole === 'ROLE_ADMIN' && (
                    <li>
                      <Link to="/admin/dashboard" onClick={() => setIsDropdownOpen(false)}>
                        Dashboard
                      </Link>
                    </li>
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
            <li>
              <Link to="/login">Login/Register</Link>
            </li>
          )}
        </ul>

        <button
          className="mobile-menu-btn"
          onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
          aria-label="Toggle navigation"
          aria-expanded={isMobileMenuOpen}
        >
          <span></span>
          <span></span>
          <span></span>
        </button>

        <div ref={mobileMenuRef} className={`mobile-menu ${isMobileMenuOpen ? 'open' : ''}`}>
          <div className="mobile-menu-content">
            {links.map((link) => (
              <Link key={link.name} to={link.path} onClick={() => setIsMobileMenuOpen(false)}>
                {link.name}
              </Link>
            ))}

            {token ? (
              <div className="mobile-account-section">
                <Link to="/account" onClick={() => setIsMobileMenuOpen(false)}>
                  Profile
                </Link>
                {user?.userRole === 'ROLE_ADMIN' && (
                  <Link to="/admin/dashboard" onClick={() => setIsMobileMenuOpen(false)}>
                    Dashboard
                  </Link>
                )}
                <button onClick={handleLogout} className="mobile-logout-btn">
                  Logout
                </button>
              </div>
            ) : (
              <Link to="/login" onClick={() => setIsMobileMenuOpen(false)}>
                Login/Register
              </Link>
            )}
          </div>
        </div>
      </nav>
    </header>
  );
};
