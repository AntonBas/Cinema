import React from 'react';
import { useAuth } from '../../context/AuthContext';
import './HomePage.css';
import { Layout } from '../../components/layout/Layout';

export const HomePage: React.FC = () => {
  const { user } = useAuth();

  return (
    <Layout>
      <div className="home-page">
        <div className="home-content">
          <h1>Welcome to Cinema System</h1>
          {user && (
            <p>Hello, {user.firstName} {user.lastName}!</p>
          )}
        </div>
      </div>
    </Layout>
  );
};