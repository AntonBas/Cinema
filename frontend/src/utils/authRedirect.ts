const REDIRECT_PARAM = "redirect";
const OAUTH2_REDIRECT_STORAGE_KEY = "postLoginRedirect";
const DEFAULT_REDIRECT = "/";
const AUTH_PAGES = ["/login", "/register", "/oauth2/redirect"];

const isAuthPage = (path: string): boolean =>
  AUTH_PAGES.some((page) => path === page || path.startsWith(`${page}?`));

export const getSafeRedirect = (target: string | null | undefined): string => {
  if (!target || !target.startsWith("/") || target.startsWith("//") || target.startsWith("/\\")) {
    return DEFAULT_REDIRECT;
  }
  return isAuthPage(target) ? DEFAULT_REDIRECT : target;
};

export const getRedirectFromSearch = (search: URLSearchParams): string =>
  getSafeRedirect(search.get(REDIRECT_PARAM));

export const buildLoginPath = (target: string): string => {
  const safeTarget = getSafeRedirect(target);
  return safeTarget === DEFAULT_REDIRECT
    ? "/login"
    : `/login?${REDIRECT_PARAM}=${encodeURIComponent(safeTarget)}`;
};

export const rememberOAuth2Redirect = (target: string): void => {
  try {
    sessionStorage.setItem(OAUTH2_REDIRECT_STORAGE_KEY, getSafeRedirect(target));
  } catch {
    return;
  }
};

export const consumeOAuth2Redirect = (): string => {
  try {
    const target = sessionStorage.getItem(OAUTH2_REDIRECT_STORAGE_KEY);
    sessionStorage.removeItem(OAUTH2_REDIRECT_STORAGE_KEY);
    return getSafeRedirect(target);
  } catch {
    return DEFAULT_REDIRECT;
  }
};
