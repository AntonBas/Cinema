import React from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { Layout } from '@/components/layout/Layout/Layout';
import styles from './HomePage.module.css';

export const HomePage: React.FC = () => {
  const { user } = useAuth();

  return (
    <Layout>
      <div className={styles.homePage}>
        <div className={styles.homeContent}>
          <h1>Welcome to Cinema System</h1>
          {user && (
            <p>Hello, {user.firstName} {user.lastName}!</p>
          )}
        </div>
      </div>
    </Layout>
  );
};