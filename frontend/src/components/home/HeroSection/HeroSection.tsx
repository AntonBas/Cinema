import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/Button/Button';
import styles from './HeroSection.module.css';

export const HeroSection: React.FC = () => {
    const navigate = useNavigate();

    return (
        <section className={styles.hero}>
            <div className={styles.heroContent}>
                <h1 className={styles.heroTitle}>
                    Experience Cinema<br />
                    <span className={styles.heroHighlight}>Like Never Before</span>
                </h1>
                <p className={styles.heroSubtitle}>
                    Immerse yourself in the ultimate movie experience with crystal clear visuals,<br />
                    powerful sound, and the most comfortable seats in town.
                </p>
                <div className={styles.heroButtons}>
                    <Button variant="primary" size="large" onClick={() => navigate('/schedule')}>
                        Book Now
                    </Button>
                </div>
                <div className={styles.heroFeatures}>
                    <div className={styles.feature}>
                        <span className={styles.featureValue}>4K</span>
                        <span className={styles.featureLabel}>Ultra HD</span>
                    </div>
                    <div className={styles.feature}>
                        <span className={styles.featureValue}>Dolby</span>
                        <span className={styles.featureLabel}>Atmos</span>
                    </div>
                    <div className={styles.feature}>
                        <span className={styles.featureValue}>Comfort</span>
                        <span className={styles.featureLabel}>Seats</span>
                    </div>
                </div>
            </div>
        </section>
    );
};