import React from "react";
import { Link } from "react-router-dom";
import { Layout } from "@/components/layout/Layout/Layout";
import { Button } from "@/components/ui/Button/Button";
import styles from "./NotFoundPage.module.css";

export const NotFoundPage: React.FC = () => {
  return (
    <Layout>
      <div className={styles.page}>
        <div className={styles.container}>
          <span className={styles.code}>404</span>
          <h1 className={styles.title}>Page Not Found</h1>
          <p className={styles.description}>
            The page you are looking for doesn&apos;t exist or has been moved.
          </p>
          <div className={styles.actions}>
            <Link to="/">
              <Button variant="primary">Go to Home</Button>
            </Link>
            <Link to="/movies/current">
              <Button variant="outline">Browse Movies</Button>
            </Link>
          </div>
        </div>
      </div>
    </Layout>
  );
};
