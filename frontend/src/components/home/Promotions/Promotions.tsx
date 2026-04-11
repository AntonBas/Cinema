import React, { useState, useEffect, useRef } from 'react';
import { Button } from '@/components/ui/Button/Button';
import type { PromotionResponse } from '@/types/promotion';
import styles from './Promotions.module.css';

interface PromotionsProps {
    promotions: PromotionResponse[];
    loading?: boolean;
    onClaim?: (promotionId: number, title: string) => Promise<void>;
    claimedPromotionIds?: number[];
}

const ITEMS_TO_SHOW = 3;
const AUTO_PLAY_INTERVAL = 5000;

const formatDate = (dateString?: string): string => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
};

const isExpired = (endDate?: string): boolean => {
    if (!endDate) return false;
    return new Date(endDate) < new Date();
};

export const Promotions: React.FC<PromotionsProps> = ({
    promotions,
    loading,
    onClaim,
    claimedPromotionIds = []
}) => {
    const [currentIndex, setCurrentIndex] = useState(0);
    const [isHovered, setIsHovered] = useState(false);
    const [claimingId, setClaimingId] = useState<number | null>(null);
    const timerRef = useRef<number | null>(null);

    const maxIndex = Math.max(0, promotions.length - ITEMS_TO_SHOW);

    const nextSlide = () => {
        setCurrentIndex(prev => (prev >= maxIndex ? 0 : prev + 1));
    };

    const prevSlide = () => {
        setCurrentIndex(prev => (prev <= 0 ? maxIndex : prev - 1));
    };

    useEffect(() => {
        if (isHovered || loading || promotions.length <= ITEMS_TO_SHOW) return;

        timerRef.current = window.setInterval(nextSlide, AUTO_PLAY_INTERVAL);

        return () => {
            if (timerRef.current) clearInterval(timerRef.current);
        };
    }, [isHovered, loading, promotions.length, maxIndex]);

    const handleClaim = async (promotionId: number, title: string) => {
        if (claimingId) return;
        setClaimingId(promotionId);
        try {
            await onClaim?.(promotionId, title);
        } finally {
            setClaimingId(null);
        }
    };

    if (loading) {
        return (
            <section className={styles.section}>
                <div className={styles.container}>
                    <div className={styles.sectionHeader}>
                        <h2 className={styles.sectionTitle}>Special Offers</h2>
                    </div>
                    <div className={styles.loading}>Loading offers...</div>
                </div>
            </section>
        );
    }

    if (!promotions.length) return null;

    const visiblePromotions = promotions.slice(currentIndex, currentIndex + ITEMS_TO_SHOW);
    const isClaimed = (id: number) => claimedPromotionIds.includes(id);

    return (
        <section
            className={styles.section}
            onMouseEnter={() => setIsHovered(true)}
            onMouseLeave={() => setIsHovered(false)}
        >
            <div className={styles.container}>
                <div className={styles.sectionHeader}>
                    <h2 className={styles.sectionTitle}>Special Offers</h2>
                </div>

                <div className={styles.carouselContainer}>
                    {promotions.length > ITEMS_TO_SHOW && (
                        <Button variant="outline" size="small" className={styles.navButton} onClick={prevSlide}>
                            &#10094;
                        </Button>
                    )}

                    <div className={styles.carouselWrapper}>
                        <div className={styles.promotionsGrid}>
                            {visiblePromotions.map(promo => {
                                const expired = isExpired(promo.endDate);
                                const claimed = isClaimed(promo.id);
                                const isClaiming = claimingId === promo.id;

                                return (
                                    <div
                                        key={promo.id}
                                        className={`${styles.promoCard} ${expired ? styles.expired : ''} ${claimed ? styles.claimed : ''}`}
                                    >
                                        <div className={styles.promoContent}>
                                            <h3 className={styles.promoTitle}>{promo.title}</h3>
                                            {promo.description && <p className={styles.promoDescription}>{promo.description}</p>}
                                            <div className={styles.promoDetails}>
                                                <span className={styles.bonusPoints}>+{promo.bonusPoints} points</span>
                                                {promo.startDate && promo.endDate && (
                                                    <span className={styles.promoDate}>
                                                        {formatDate(promo.startDate)} - {formatDate(promo.endDate)}
                                                    </span>
                                                )}
                                            </div>
                                        </div>
                                        <div className={styles.promoFooter}>
                                            {onClaim && (
                                                <Button
                                                    variant={claimed ? 'success' : 'primary'}
                                                    size="medium"
                                                    onClick={() => handleClaim(promo.id, promo.title)}
                                                    disabled={expired || claimed || isClaiming}
                                                    loading={isClaiming}
                                                >
                                                    {claimed ? '✓ Claimed' : expired ? 'Expired' : 'Claim'}
                                                </Button>
                                            )}
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    </div>

                    {promotions.length > ITEMS_TO_SHOW && (
                        <Button variant="outline" size="small" className={styles.navButton} onClick={nextSlide}>
                            &#10095;
                        </Button>
                    )}
                </div>

                {promotions.length > ITEMS_TO_SHOW && (
                    <div className={styles.dots}>
                        {Array.from({ length: maxIndex + 1 }).map((_, idx) => (
                            <button
                                key={idx}
                                className={`${styles.dot} ${currentIndex === idx ? styles.dotActive : ''}`}
                                onClick={() => setCurrentIndex(idx)}
                            />
                        ))}
                    </div>
                )}
            </div>
        </section>
    );
};