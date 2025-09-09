import React from 'react';
import './Footer.css';

export const Footer: React.FC = () => {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="footer">
      <div className="footer-content">
        <div className="footer-section">
          <h3>Cinema</h3>
          <p>Your ultimate movie experience</p>
          <div className="social-links">
            <a href="#" className="social-link" aria-label="Movies">🎬</a>
            <a href="#" className="social-link" aria-label="TV Shows">📺</a>
            <a href="#" className="social-link" aria-label="Theater">🎭</a>
          </div>
        </div>

        <div className="footer-section">
          <h4>Quick Links</h4>
          <ul>
            <li><a href="/">Home</a></li>
            <li><a href="/movies">Movies</a></li>
            <li><a href="/schedule">Schedule</a></li>
            <li><a href="/about">About Us</a></li>
          </ul>
        </div>

        <div className="footer-section">
          <h4>Contact</h4>
          <ul>
            <li>📧 basantonoleg@gmail.com</li>
            <li>📞 +380 (96) 179-4151</li>
            <li>📍 Lviv, Ukraine</li>
          </ul>
        </div>

        <div className="footer-section">
          <h4>Follow Us</h4>
          {/* <div className="social-icons">
            <a href="#" className="social-icon" aria-label="Facebook">📘</a>
            <a href="#" className="social-icon" aria-label="Instagram">📸</a>
            <a href="#" className="social-icon" aria-label="Twitter">🐦</a>
          </div> */}
        </div>
      </div>

      <div className="footer-bottom">
        <div className="footer-developer">
          <span>Developed by </span>
          <a href="https://www.linkedin.com/in/anton-bas-244465169/" target="_blank" rel="noopener noreferrer" className="developer-name">Anton Bas </a>
        </div>
        <div className="footer-copyright">
          © {currentYear} Cinema. All rights reserved.
        </div>
      </div>
    </footer>
  );
};
