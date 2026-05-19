import React, { useEffect, useState } from "react";
import { Layout } from "@/components/layout/Layout/Layout";
import { HeroSection } from "@/components/home/HeroSection/HeroSection";
import { NowShowing } from "@/components/home/NowShowing/NowShowing";
import { ComingSoon } from "@/components/home/ComingSoon/ComingSoon";
import { LeavingSoon } from "@/components/home/LeavingSoon/LeavingSoon";
import { Promotions } from "@/components/home/Promotions/Promotions";
import { useMovies } from "@/hooks/features/movies/useMovies";
import { usePromotion } from "@/hooks/features/promotion/usePromotion";
import { useAuth } from "@/context/AuthContext";
import LoadingSpinner from "@/components/ui/LoadingSpinner/LoadingSpinner";
import styles from "./HomePage.module.css";

export const HomePage: React.FC = () => {
  const { isAuthenticated } = useAuth();
  const {
    currentMoviesHome,
    upcomingMoviesHome,
    leavingSoonHome,
    loading: moviesLoading,
    getCurrentMoviesForHome,
    getUpcomingMoviesForHome,
    getLeavingSoonForHome,
  } = useMovies();

  const {
    availablePromotions,
    claimedPromotions,
    loading: promotionsLoading,
    getAvailable,
    getClaimed,
    claim,
  } = usePromotion();

  const [claimedIds, setClaimedIds] = useState<number[]>([]);

  const loading = moviesLoading || promotionsLoading;

  useEffect(() => {
    getCurrentMoviesForHome();
    getUpcomingMoviesForHome();
    getLeavingSoonForHome();
    getAvailable();
    if (isAuthenticated) {
      getClaimed();
    }
  }, []);

  useEffect(() => {
    if (claimedPromotions.length > 0) {
      setClaimedIds(claimedPromotions.map((p) => p.id));
    }
  }, [claimedPromotions]);

  const handleClaimPromotion = async (promotionId: number, _title: string) => {
    const result = await claim({ promotionId });
    if (result) {
      setClaimedIds((prev) => [...prev, promotionId]);
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

  const hasContent =
    currentMoviesHome.length > 0 ||
    upcomingMoviesHome.length > 0 ||
    leavingSoonHome.length > 0;

  return (
    <Layout>
      <HeroSection />
      {currentMoviesHome.length > 0 && (
        <NowShowing movies={currentMoviesHome} />
      )}
      {upcomingMoviesHome.length > 0 && (
        <ComingSoon movies={upcomingMoviesHome} />
      )}
      {leavingSoonHome.length > 0 && <LeavingSoon movies={leavingSoonHome} />}
      {availablePromotions.length > 0 && (
        <Promotions
          promotions={availablePromotions}
          onClaim={handleClaimPromotion}
          claimedPromotionIds={claimedIds}
        />
      )}
    </Layout>
  );
};
