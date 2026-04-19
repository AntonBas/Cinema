import React, { useState, useEffect, useRef } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";
import styles from "./Header.module.css";

interface NavLink {
  name: string;
  path: string;
}

const LINKS: NavLink[] = [
  { name: "Home", path: "/" },
  { name: "Movies", path: "/movies" },
  { name: "Schedule", path: "/schedule" },
];

export const Header: React.FC = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const dropdownRef = useRef<HTMLLIElement>(null);
  const mobileMenuRef = useRef<HTMLDivElement>(null);

  const isActiveLink = (path: string) => location.pathname === path;

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node)
      ) {
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
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleLogout = () => {
    logout();
    setIsDropdownOpen(false);
    setIsMobileMenuOpen(false);
    navigate("/");
  };

  const closeMobileMenu = () => setIsMobileMenuOpen(false);

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
          {LINKS.map((link) => (
            <li key={link.name}>
              <Link
                to={link.path}
                className={isActiveLink(link.path) ? styles.active : ""}
                onClick={() => setIsDropdownOpen(false)}
              >
                {link.name}
              </Link>
            </li>
          ))}

          {isAuthenticated ? (
            <li className={styles.dropdown} ref={dropdownRef}>
              <button
                className={styles.dropdownBtn}
                onClick={() => setIsDropdownOpen(!isDropdownOpen)}
              >
                <span className={styles.userName}>
                  {user?.firstName || "User"}
                </span>
                <span
                  className={`${styles.dropdownArrow} ${isDropdownOpen ? styles.open : ""}`}
                >
                  ▼
                </span>
              </button>
              {isDropdownOpen && (
                <ul className={styles.dropdownMenu}>
                  <li className={styles.userInfoItem}>
                    <div className={styles.userInfo}>
                      <strong>
                        {user?.firstName} {user?.lastName}
                      </strong>
                      <span className={styles.userEmail}>{user?.email}</span>
                    </div>
                  </li>
                  <li className={styles.dropdownDivider} />
                  <li>
                    <Link
                      to="/account"
                      onClick={() => setIsDropdownOpen(false)}
                    >
                      Profile
                    </Link>
                  </li>
                  {user?.userRole === "ROLE_ADMIN" && (
                    <li>
                      <Link
                        to="/admin/movies"
                        onClick={() => setIsDropdownOpen(false)}
                      >
                        Admin Panel
                      </Link>
                    </li>
                  )}
                  <li className={styles.dropdownDivider} />
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
                className={isActiveLink("/login") ? styles.active : ""}
              >
                Login
              </Link>
            </li>
          )}
        </ul>

        <button
          className={`${styles.mobileMenuBtn} ${isMobileMenuOpen ? styles.open : ""}`}
          onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
        >
          <span />
          <span />
          <span />
        </button>

        <div
          ref={mobileMenuRef}
          className={`${styles.mobileMenu} ${isMobileMenuOpen ? styles.open : ""}`}
        >
          <div className={styles.mobileMenuContent}>
            {LINKS.map((link) => (
              <Link
                key={link.name}
                to={link.path}
                className={isActiveLink(link.path) ? styles.active : ""}
                onClick={closeMobileMenu}
              >
                {link.name}
              </Link>
            ))}

            {isAuthenticated ? (
              <div className={styles.mobileAccountSection}>
                <div className={styles.mobileUserInfo}>
                  <div>
                    <strong>
                      {user?.firstName} {user?.lastName}
                    </strong>
                    <span className={styles.userEmail}>{user?.email}</span>
                  </div>
                </div>
                <Link to="/account" onClick={closeMobileMenu}>
                  Profile
                </Link>
                {user?.userRole === "ROLE_ADMIN" && (
                  <Link to="/admin/dashboard" onClick={closeMobileMenu}>
                    Dashboard
                  </Link>
                )}
                <button
                  onClick={handleLogout}
                  className={styles.mobileLogoutBtn}
                >
                  Logout
                </button>
              </div>
            ) : (
              <Link
                to="/login"
                className={isActiveLink("/login") ? styles.active : ""}
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
