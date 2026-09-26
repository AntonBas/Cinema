import { API_BASE_URL } from "@/services/api";

export const DEFAULT_POSTER_URL = "/images/default-movie-poster.svg";

export const resolvePosterUrl = (url: string | undefined | null): string => {
  if (!url || url.trim() === "") return DEFAULT_POSTER_URL;
  if (/^https?:\/\//i.test(url)) return url;
  return `${API_BASE_URL}${url.startsWith("/") ? url : `/${url}`}`;
};
