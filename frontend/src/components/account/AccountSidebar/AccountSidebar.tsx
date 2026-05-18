import React, { useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { Menu, X } from "lucide-react";
import styles from "./AccountSidebar.module.css";

interface SidebarItem {
  id: string;
  label: string;
  path: string;
}

const SIDEBAR_ITEMS: SidebarItem[] = [
  { id: "overview", label: "Overview", path: "/account" },
  { id: "tickets", label: "My Tickets", path: "/account/tickets" },
  { id: "bonuses", label: "Bonuses", path: "/account/bonuses" },
  { id: "security", label: "Security", path: "/account/security" },
];

export const AccountSidebar: React.FC = () => {
  const location = useLocation();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const closeMobileMenu = () => setIsMobileMenuOpen(false);

  return (
    <>
      <button
        className={styles.mobileMenuButton}
        onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
      >
        {isMobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
        <span>Account Menu</span>
      </button>

      <aside
        className={`${styles.sidebar} ${isMobileMenuOpen ? styles.sidebarOpen : ""}`}
      >
        <div className={styles.sidebarHeader}>
          <h2 className={styles.sidebarTitle}>My Account</h2>
        </div>

        <nav className={styles.sidebarNav}>
          <ul className={styles.sidebarList}>
            {SIDEBAR_ITEMS.map((item) => (
              <li key={item.id} className={styles.sidebarItem}>
                <Link
                  to={item.path}
                  className={`${styles.sidebarLink} ${location.pathname === item.path ? styles.active : ""}`}
                  onClick={closeMobileMenu}
                >
                  <span className={styles.sidebarLabel}>{item.label}</span>
                </Link>
              </li>
            ))}
          </ul>
        </nav>
      </aside>
    </>
  );
};
