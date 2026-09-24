import React from "react";
import { NavLink, Outlet } from "react-router-dom";
import { Layout } from "@/components/layout/Layout/Layout";
import { PageHeader } from "@/components/ui/PageHeader/PageHeader";
import styles from "./MoviesLayout.module.css";

const navItems = [
  { path: "/movies/current", label: "Now Showing" },
  { path: "/movies/upcoming", label: "Coming Soon" },
];

export const MoviesLayout: React.FC = () => {
  return (
    <Layout>
      <div className={styles.layout}>
        <div className={styles.navContainer}>
          <div className={styles.header}>
            <PageHeader
              align="center"
              title="Movies"
              subtitle="Discover films now showing and coming soon"
              className={styles.pageHeader}
            />
            <nav className={styles.nav}>
              <div className={styles.navInner}>
                {navItems.map(({ path, label }) => (
                  <NavLink
                    key={path}
                    to={path}
                    className={({ isActive }) =>
                      `${styles.navLink} ${isActive ? styles.active : ""}`
                    }
                  >
                    <span className={styles.navText}>{label}</span>
                  </NavLink>
                ))}
              </div>
            </nav>
          </div>
        </div>

        <main className={styles.content}>
          <Outlet />
        </main>
      </div>
    </Layout>
  );
};
