import React, { useEffect, useState } from "react";
import { Layout } from "@/components/layout/Layout/Layout";
import { HeroSection } from "@/components/home/HeroSection/HeroSection";
import { MovieRail } from "@/components/home/MovieRail/MovieRail";
import { Promotions } from "@/components/home/Promotions/Promotions";
import { useMovie } from "@/hooks/features/movie/useMovie";
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
  } = useMovie();

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
    getCurrentMoviesForHome().catch(() => {});
    getUpcomingMoviesForHome().catch(() => {});
    getLeavingSoonForHome().catch(() => {});
    getAvailable().catch(() => {});
    if (isAuthenticated) {
      getClaimed().catch(() => {});
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
      <MovieRail
        title="Now Showing"
        movies={currentMoviesHome}
        loading={moviesLoading}
        viewAllPath="/movies/current"
      />
      <MovieRail
        title="Coming Soon"
        movies={upcomingMoviesHome}
        loading={moviesLoading}
        viewAllPath="/movies/upcoming"
        highlighted
      />
      <MovieRail
        title="Last Chance"
        movies={leavingSoonHome}
        loading={moviesLoading}
        highlighted
      />
      <Promotions
        promotions={availablePromotions}
        loading={promotionsLoading}
        onClaim={handleClaimPromotion}
        claimedPromotionIds={claimedIds}
      />
    </Layout>
  );
};
