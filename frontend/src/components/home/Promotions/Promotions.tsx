import React, { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/Button/Button";
import { useAuth } from "@/context/AuthContext";
import type { PromotionResponse } from "@/types/promotion";
import styles from "./Promotions.module.css";

interface PromotionsProps {
  promotions: PromotionResponse[];
  loading?: boolean;
  onClaim?: (promotionId: number, title: string) => Promise<void>;
  claimedPromotionIds?: number[];
}

const AUTO_PLAY_INTERVAL = 5000;

const formatDate = (dateString?: string): string => {
  if (!dateString) return "";
  return new Date(dateString).toLocaleDateString("en-US", {
    month: "short",
    day: "numeric",
  });
};

const isExpired = (endDate?: string): boolean => {
  if (!endDate) return false;
  return new Date(endDate) < new Date();
};

const getInitialItemsToShow = () => {
  return window.innerWidth <= 768 ? 1 : 3;
};

export const Promotions: React.FC<PromotionsProps> = ({
  promotions,
  loading,
  onClaim,
  claimedPromotionIds = [],
}) => {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isHovered, setIsHovered] = useState(false);
  const [claimingId, setClaimingId] = useState<number | null>(null);
  const [itemsToShow, setItemsToShow] = useState(getInitialItemsToShow);
  const timerRef = useRef<number | null>(null);

  useEffect(() => {
    const handleResize = () => {
      setItemsToShow(window.innerWidth <= 768 ? 1 : 3);
    };

    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  const maxIndex = Math.max(0, promotions.length - itemsToShow);

  const nextSlide = () => {
    setCurrentIndex((prev) => (prev >= maxIndex ? 0 : prev + 1));
  };

  const prevSlide = () => {
    setCurrentIndex((prev) => (prev <= 0 ? maxIndex : prev - 1));
  };

  useEffect(() => {
    if (isHovered || loading || promotions.length <= itemsToShow) return;

    timerRef.current = window.setInterval(nextSlide, AUTO_PLAY_INTERVAL);

    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, [isHovered, loading, promotions.length, maxIndex, itemsToShow]);

  const handleClaim = async (promotionId: number, title: string) => {
    if (!isAuthenticated || claimingId) return;
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

  const showCarousel = promotions.length > itemsToShow;
  const visiblePromotions = showCarousel
    ? promotions.slice(currentIndex, currentIndex + itemsToShow)
    : promotions;
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
          {showCarousel && (
            <Button
              variant="outline"
              size="small"
              className={styles.navButton}
              onClick={prevSlide}
            >
              &#10094;
            </Button>
          )}

          <div className={styles.carouselWrapper}>
            <div
              className={`${styles.promotionsGrid} ${!showCarousel ? styles.promotionsGridCentered : ""}`}
            >
              {visiblePromotions.map((promo) => {
                const expired = isExpired(promo.endDate);
                const claimed = isClaimed(promo.id);
                const isClaiming = claimingId === promo.id;

                return (
                  <div
                    key={promo.id}
                    className={`${styles.promoCard} ${expired ? styles.expired : ""} ${claimed ? styles.claimed : ""}`}
                  >
                    <div className={styles.promoContent}>
                      <h3 className={styles.promoTitle}>{promo.title}</h3>
                      {promo.description && (
                        <p className={styles.promoDescription}>
                          {promo.description}
                        </p>
                      )}
                      <div className={styles.promoDetails}>
                        <span className={styles.bonusPoints}>
                          +{promo.bonusPoints} points
                        </span>
                        {promo.startDate && promo.endDate && (
                          <span className={styles.promoDate}>
                            {formatDate(promo.startDate)} -{" "}
                            {formatDate(promo.endDate)}
                          </span>
                        )}
                      </div>
                    </div>
                    <div className={styles.promoFooter}>
                      {isAuthenticated && onClaim && (
                        <Button
                          variant={claimed ? "success" : "primary"}
                          size="medium"
                          onClick={() => handleClaim(promo.id, promo.title)}
                          disabled={expired || claimed || isClaiming}
                          loading={isClaiming}
                        >
                          {claimed
                            ? "✓ Claimed"
                            : expired
                              ? "Expired"
                              : "Claim"}
                        </Button>
                      )}
                      {!isAuthenticated && (
                        <Button
                          variant="outline"
                          size="medium"
                          onClick={() => navigate("/login")}
                        >
                          Login to Claim
                        </Button>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {showCarousel && (
            <Button
              variant="outline"
              size="small"
              className={styles.navButton}
              onClick={nextSlide}
            >
              &#10095;
            </Button>
          )}
        </div>

        {showCarousel && (
          <div className={styles.dots}>
            {Array.from({ length: maxIndex + 1 }).map((_, idx) => (
              <button
                key={idx}
                className={`${styles.dot} ${currentIndex === idx ? styles.dotActive : ""}`}
                onClick={() => setCurrentIndex(idx)}
              />
            ))}
          </div>
        )}
      </div>
    </section>
  );
};
