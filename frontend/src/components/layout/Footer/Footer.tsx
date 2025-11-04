import React from 'react';
import styles from './Footer.module.css';

export const Footer: React.FC = () => {
  const currentYear = new Date().getFullYear();

  return (
    <footer className={styles.footer}>
      <div className={styles.footerContent}>
        <div className={styles.footerSection}>
          <h3>Cinema</h3>
          <p>Your ultimate movie experience</p>
          <div className={styles.socialLinks}>
            <a href="#" className={styles.socialLink} aria-label="Movies">🎬</a>
            <a href="#" className={styles.socialLink} aria-label="TV Shows">📺</a>
            <a href="#" className={styles.socialLink} aria-label="Theater">🎭</a>
          </div>
        </div>

        <div className={styles.footerSection}>
          <h4>Quick Links</h4>
          <ul>
            <li><a href="/">Home</a></li>
            <li><a href="/movies">Movies</a></li>
            <li><a href="/schedule">Schedule</a></li>
            <li><a href="/about">About Us</a></li>
          </ul>
        </div>

        <div className={styles.footerSection}>
          <h4>Contact</h4>
          <ul>
            <li>📧 basantonoleg@gmail.com</li>
            <li>📞 +380 (96) 179-4151</li>
            <li>📍 Lviv, Ukraine</li>
          </ul>
        </div>

        <div className={styles.footerSection}>
          <h4>Follow Us</h4>
          {/* <div className={styles.socialIcons}>
            <a href="#" className={styles.socialIcon} aria-label="Facebook">📘</a>
            <a href="#" className={styles.socialIcon} aria-label="Instagram">📸</a>
            <a href="#" className={styles.socialIcon} aria-label="Twitter">🐦</a>
          </div> */}
        </div>
      </div>

      <div className={styles.footerBottom}>
        <div className={styles.footerDeveloper}>
          <span>Developed by </span>
          <a href="https://www.linkedin.com/in/anton-bas-244465169/" target="_blank" rel="noopener noreferrer" className={styles.developerName}>Anton Bas </a>
        </div>
        <div className={styles.footerCopyright}>
          © {currentYear} Cinema. All rights reserved.
        </div>
      </div>
    </footer>
  );
};