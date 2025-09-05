import React from 'react';
import { Link } from 'react-router-dom';
import './HomePage.css';

const HomePage: React.FC = () => {
  const user = JSON.parse(localStorage.getItem('user') || 'null');

  return (
    <div className="home-page">
      <h1>Ласкаво просимо!</h1>
      
      {user ? (
        <div className="welcome-message">
          <p>Ви увійшли як: <strong>{user.username}</strong></p>
          <p>Оберіть розділ для роботи:</p>
          <div className="admin-links">
            <Link to="/admin/movie" className="admin-link">
              Управління фільмами
            </Link>
            <Link to="/admin/person" className="admin-link">
              Управління персонами
            </Link>
          </div>
        </div>
      ) : (
        <div className="guest-message">
          <p>Будь ласка, увійдіть в систему для доступу до адмін-панелі</p>
          <Link to="/login" className="login-link">
            Увійти
          </Link>
        </div>
      )}
    </div>
  );
};

export default HomePage;