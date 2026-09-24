import { useCallback, useMemo } from "react";
import { useSearchParams } from "react-router-dom";

export type UrlParamValue = string | number | boolean | null | undefined;

interface UpdateOptions {
  replace?: boolean;
  reset?: boolean;
}

const parsePage = (value: string | null): number => {
  const pageNumber = Number(value);
  return Number.isInteger(pageNumber) && pageNumber > 1 ? pageNumber - 1 : 0;
};

export const parseOptionalEnumParam = <T extends string>(
  value: string | undefined,
  allowed: ReadonlyArray<T>,
): T | undefined => {
  const normalized = value?.toUpperCase();
  return allowed.find((item) => item.toUpperCase() === normalized);
};

export const parseEnumParam = <T extends string>(
  value: string | undefined,
  allowed: ReadonlyArray<T>,
  fallback: T,
): T => parseOptionalEnumParam(value, allowed) ?? fallback;

export const parseNumberParam = (
  value: string | undefined,
): number | undefined => {
  const parsed = Number(value);
  return value && Number.isInteger(parsed) ? parsed : undefined;
};

export const parseBooleanParam = (
  value: string | undefined,
): boolean | undefined =>
  value === "true" ? true : value === "false" ? false : undefined;

export const toUrlEnumValue = <T extends string>(
  value: T | undefined,
  fallback?: T,
): string | undefined =>
  value === undefined || value === fallback ? undefined : value.toLowerCase();

export const useUrlParams = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  const page = parsePage(searchParams.get("page"));
  const query = searchParams.get("q") || undefined;
  const sort = searchParams.get("sort") || undefined;

  const getParam = useCallback(
    (key: string) => searchParams.get(key) || undefined,
    [searchParams],
  );

  const setParams = useCallback(
    (changes: Record<string, UrlParamValue>, options: UpdateOptions = {}) => {
      setSearchParams(
        (previous) => {
          const next = new URLSearchParams(
            options.reset ? undefined : previous,
          );
          Object.entries(changes).forEach(([key, value]) => {
            if (value === undefined || value === null || value === "") {
              next.delete(key);
            } else {
              next.set(key, String(value));
            }
          });
          return next;
        },
        { replace: options.replace },
      );
    },
    [setSearchParams],
  );

  const setFilters = useCallback(
    (changes: Record<string, UrlParamValue>, options: UpdateOptions = {}) => {
      setParams({ ...changes, page: undefined }, options);
    },
    [setParams],
  );

  const clearParams = useCallback(() => {
    setParams({}, { reset: true });
  }, [setParams]);

  const setPage = useCallback(
    (newPage: number) => {
      setParams({ page: newPage > 0 ? newPage + 1 : undefined });
    },
    [setParams],
  );

  const setSearch = useCallback(
    (search: string) => {
      setFilters({ q: search.trim() }, { replace: true });
    },
    [setFilters],
  );

  const setSort = useCallback(
    (newSort: string) => {
      setFilters({ sort: newSort });
    },
    [setFilters],
  );

  return useMemo(
    () => ({
      page,
      query,
      sort,
      getParam,
      setParams,
      setFilters,
      clearParams,
      setPage,
      setSearch,
      setSort,
    }),
    [
      page,
      query,
      sort,
      getParam,
      setParams,
      setFilters,
      clearParams,
      setPage,
      setSearch,
      setSort,
    ],
  );
};
