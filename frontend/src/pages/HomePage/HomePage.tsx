import React from 'react';
import { Header } from '../../components/layout/Header';
import { useAuth } from '../../context/AuthContext';
import './HomePage.css';

export const HomePage: React.FC = () => {
  const { user } = useAuth();

  return (
    <div className="home-page">
      <Header />
      <div className="home-content">
        <h1>Welcome to Cinema System</h1>
        {user && (
          <p>Hello, {user.firstName} {user.lastName}!</p>
        )}
      </div>
    </div>
  );
};