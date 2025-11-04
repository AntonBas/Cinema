import React, { useState, useEffect, useRef } from 'react';
import { useAuth } from '@/hooks/features/auth';
import { Link } from 'react-router-dom';
import styles from './Header.module.css';

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

  return (
    <header className={styles.headerMain}>
      <nav className={styles.headerNavbar}>
        <div className={styles.logo}>
          <Link to="/">Cinema</Link>
        </div>

        <ul className={styles.navLinks}>
          {links.map((link) => (
            <li key={link.name}>
              <Link to={link.path} onClick={() => setIsDropdownOpen(false)}>
                {link.name}
              </Link>
            </li>
          ))}

          {token ? (
            <li className={styles.dropdown} ref={dropdownRef}>
              <button
                className={styles.dropdownBtn}
                onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                aria-haspopup="true"
                aria-expanded={isDropdownOpen}
              >
                My Account <span className={`${styles.dropdownArrow} ${isDropdownOpen ? styles.open : ''}`}>▼</span>
              </button>
              {isDropdownOpen && (
                <ul className={styles.dropdownMenu}>
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
                    <button onClick={handleLogout} className={styles.logoutBtn}>
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
          className={styles.mobileMenuBtn}
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
              <Link key={link.name} to={link.path} onClick={() => setIsMobileMenuOpen(false)}>
                {link.name}
              </Link>
            ))}

            {token ? (
              <div className={styles.mobileAccountSection}>
                <Link to="/account" onClick={() => setIsMobileMenuOpen(false)}>
                  Profile
                </Link>
                {user?.userRole === 'ROLE_ADMIN' && (
                  <Link to="/admin/dashboard" onClick={() => setIsMobileMenuOpen(false)}>
                    Dashboard
                  </Link>
                )}
                <button onClick={handleLogout} className={styles.mobileLogoutBtn}>
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