import React, { useState, useEffect, useRef } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useAuthActions } from '@/hooks/features/auth/useAuthActions';
import { Link, useLocation } from 'react-router-dom';
import styles from './Header.module.css';

interface NavLink {
  name: string;
  path: string;
  adminOnly?: boolean;
}

export const Header: React.FC = () => {
  const { user, isAuthenticated } = useAuth();
  const { logout } = useAuthActions();
  const location = useLocation();
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const dropdownRef = useRef<HTMLLIElement>(null);
  const mobileMenuRef = useRef<HTMLDivElement>(null);

  const links: NavLink[] = [
    { name: 'Home', path: '/' },
    { name: 'Movies', path: '/movies' },
    { name: 'Schedule', path: '/schedule' },
  ];

  const isActiveLink = (path: string) => {
    return location.pathname === path;
  };

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsDropdownOpen(false);
      }
      if (
        mobileMenuRef.current &&
        !mobileMenuRef.current.contains(event.target as Node) &&
        !(event.target as Element).closest(`.${styles.mobileMenuBtn}`)
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

  const closeMobileMenu = () => {
    setIsMobileMenuOpen(false);
  };

  return (
    <header className={styles.headerMain}>
      <nav className={styles.headerNavbar}>
        <div className={styles.logo}>
          <Link to="/" onClick={closeMobileMenu}>
            <span className={styles.logoIcon}>🎬</span>
            Cinema
          </Link>
        </div>

        <ul className={styles.navLinks}>
          {links.map((link) => (
            <li key={link.name}>
              <Link
                to={link.path}
                className={`${isActiveLink(link.path) ? styles.active : ''}`}
                onClick={() => setIsDropdownOpen(false)}
              >
                {link.name}
                {isActiveLink(link.path) && <span className={styles.activeIndicator}></span>}
              </Link>
            </li>
          ))}

          {isAuthenticated ? (
            <li className={styles.dropdown} ref={dropdownRef}>
              <button
                className={styles.dropdownBtn}
                onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                aria-haspopup="true"
                aria-expanded={isDropdownOpen}
              >
                <span className={styles.userName}>
                  {user?.firstName || 'User'}
                </span>
                <span className={`${styles.dropdownArrow} ${isDropdownOpen ? styles.open : ''}`}>▼</span>
              </button>
              {isDropdownOpen && (
                <ul className={styles.dropdownMenu}>
                  <li className={styles.userInfoItem}>
                    <div className={styles.userInfo}>
                      <strong>{user?.firstName} {user?.lastName}</strong>
                      <span className={styles.userEmail}>{user?.email}</span>
                    </div>
                  </li>
                  <li className={styles.dropdownDivider}></li>
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
                  <li className={styles.dropdownDivider}></li>
                  <li>
                    <button onClick={handleLogout} className={styles.logoutBtn}>
                      Logout
                    </button>
                  </li>
                </ul>
              )}
            </li>
          ) : (
            <li>
              <Link
                to="/login"
                className={`${isActiveLink('/login') ? styles.active : ''}`}
              >
                Login
                {isActiveLink('/login') && <span className={styles.activeIndicator}></span>}
              </Link>
            </li>
          )}
        </ul>

        <button
          className={`${styles.mobileMenuBtn} ${isMobileMenuOpen ? styles.open : ''}`}
          onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
          aria-label="Toggle navigation"
          aria-expanded={isMobileMenuOpen}
        >
          <span></span>
          <span></span>
          <span></span>
        </button>

        <div ref={mobileMenuRef} className={`${styles.mobileMenu} ${isMobileMenuOpen ? styles.open : ''}`}>
          <div className={styles.mobileMenuContent}>
            {links.map((link) => (
              <Link
                key={link.name}
                to={link.path}
                className={`${isActiveLink(link.path) ? styles.active : ''}`}
                onClick={closeMobileMenu}
              >
                {link.name}
              </Link>
            ))}

            {isAuthenticated ? (
              <div className={styles.mobileAccountSection}>
                <div className={styles.mobileUserInfo}>
                  <div>
                    <strong>{user?.firstName} {user?.lastName}</strong>
                    <span className={styles.userEmail}>{user?.email}</span>
                  </div>
                </div>
                <Link to="/account" onClick={closeMobileMenu}>
                  Profile
                </Link>
                {user?.userRole === 'ROLE_ADMIN' && (
                  <Link to="/admin/dashboard" onClick={closeMobileMenu}>
                    Dashboard
                  </Link>
                )}
                <button onClick={handleLogout} className={styles.mobileLogoutBtn}>
                  Logout
                </button>
              </div>
            ) : (
              <Link
                to="/login"
                className={`${isActiveLink('/login') ? styles.active : ''}`}
                onClick={closeMobileMenu}
              >
                Login
              </Link>
            )}
          </div>
        </div>
      </nav>
    </header>
  );
};