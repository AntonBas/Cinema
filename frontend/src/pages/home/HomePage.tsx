import React, { useState, useEffect } from 'react';
import { Layout } from '@/components/layout/Layout/Layout';
import { HeroSection } from '@/components/home/HeroSection/HeroSection';
import { NowShowing } from '@/components/home/NowShowing/NowShowing';
import { ComingSoon } from '@/components/home/ComingSoon/ComingSoon';
import { LeavingSoon } from '@/components/home/LeavingSoon/LeavingSoon';
import { Promotions } from '@/components/home/Promotions/Promotions';
import { useMovies } from '@/hooks/features/movies/useMovies';
import { usePromotion } from '@/hooks/features/promotion/usePromotion';
import { useNotification } from '@/hooks/common/useNotification';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import type { PromotionResponse } from '@/types/promotion';
import styles from './HomePage.module.css';

export const HomePage: React.FC = () => {
  const {
    nowShowingHome,
    comingSoonHome,
    leavingSoonHome,
    getNowShowingForHome,
    getComingSoonForHome,
    getLeavingSoonForHome,
    loading: moviesLoading
  } = useMovies();
  const {
    getAvailable,
    getClaimed,
    claimPromotion,
    loading: promotionsLoading,
    claimedPromotions
  } = usePromotion();
  const { showNotification } = useNotification();

  const [promotions, setPromotions] = useState<PromotionResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchHomeData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [availablePromotions] = await Promise.all([
        getAvailable(),
        getClaimed(),
        getNowShowingForHome(),
        getComingSoonForHome(),
        getLeavingSoonForHome()
      ]);

      setPromotions(availablePromotions || []);
    } catch (err) {
      console.error('Failed to load home data:', err);
      setError(err instanceof Error ? err.message : 'Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHomeData();
  }, []);

  const handleClaimPromotion = async (promotionId: number, title: string) => {
    try {
      await claimPromotion({ promotionId }, title);
      showNotification(`Promotion "${title}" claimed successfully!`, 'success');
      await getClaimed();
    } catch (error) {
      showNotification('Failed to claim promotion', 'error');
    }
  };

  if (loading) {
    return (
      <Layout>
        <div className={styles.loading}>
          <LoadingSpinner text="Loading..." />
        </div>
      </Layout>
    );
  }

  if (error) {
    return (
      <Layout>
        <div className={styles.error}>
          <p>Error loading page: {error}</p>
          <button onClick={() => window.location.reload()}>Retry</button>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <HeroSection />
      {nowShowingHome.length > 0 && <NowShowing movies={nowShowingHome} loading={moviesLoading} />}
      {comingSoonHome.length > 0 && <ComingSoon movies={comingSoonHome} loading={moviesLoading} />}
      {leavingSoonHome.length > 0 && <LeavingSoon movies={leavingSoonHome} loading={moviesLoading} />}
      {promotions.length > 0 && (
        <Promotions
          promotions={promotions}
          loading={promotionsLoading}
          onClaim={handleClaimPromotion}
          claimedPromotionIds={claimedPromotions.map(p => p.id)}
        />
      )}
      {nowShowingHome.length === 0 && comingSoonHome.length === 0 && leavingSoonHome.length === 0 && (
        <div className={styles.empty}>
          <p>No movies available at the moment.</p>
        </div>
      )}
    </Layout>
  );
};