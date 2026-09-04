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

  useEffect(() => {
    getCurrentMoviesForHome();
    getUpcomingMoviesForHome();
    getLeavingSoonForHome();
    getAvailable();
    if (isAuthenticated) {
      getClaimed();
    }
  }, [
    isAuthenticated,
    getCurrentMoviesForHome,
    getUpcomingMoviesForHome,
    getLeavingSoonForHome,
    getAvailable,
    getClaimed,
  ]);

  useEffect(() => {
    if (claimedPromotions.length > 0) {
      setClaimedIds(claimedPromotions.map((p) => p.id));
    }
  }, [claimedPromotions]);

  const handleClaimPromotion = async (promotionId: number) => {
    const result = await claim({ promotionId });
    if (result) {
      setClaimedIds((prev) => [...prev, promotionId]);
    }
  };

  return (
    <Layout>
      <HeroSection />
      <NowShowing movies={currentMoviesHome} loading={moviesLoading} />
      <ComingSoon movies={upcomingMoviesHome} loading={moviesLoading} />
      <LeavingSoon movies={leavingSoonHome} loading={moviesLoading} />
      <Promotions
        promotions={availablePromotions}
        loading={promotionsLoading}
        onClaim={handleClaimPromotion}
        claimedPromotionIds={claimedIds}
      />
    </Layout>
  );
};
