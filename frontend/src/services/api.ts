import axios from "axios";
import { ApiErrorException } from "@/utils/apiErrorHandler";
import { buildLoginPath } from "@/utils/authRedirect";

interface ApiErrorResponse {
  status: string;
  statusCode: number;
  message: string;
  timestamp: string;
  errors?: string[];
}

declare module "axios" {
  export interface AxiosRequestConfig {
    _sessionRechecked?: boolean;
  }
}

let isRedirecting = false;
let onUnauthorized: (() => void) | null = null;

export const setUnauthorizedHandler = (handler: (() => void) | null) => {
  onUnauthorized = handler;
};

export const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

export const api = axios.create({
  baseURL: "",
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
    "X-Requested-With": "XMLHttpRequest",
  },
  timeout: 10000,
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (
      originalRequest?.url?.includes("/api/auth/login") ||
      originalRequest?.url?.includes("/api/auth/register") ||
      originalRequest?.url?.includes("/api/auth/me")
    ) {
      if (error.response) {
        const responseData = error.response.data as ApiErrorResponse;
        if (
          responseData &&
          typeof responseData === "object" &&
          "statusCode" in responseData
        ) {
          const apiErrorException = new ApiErrorException(responseData);
          return Promise.reject(apiErrorException);
        }
      }
      return Promise.reject(error);
    }

    if (
      error.response?.status === 401 &&
      originalRequest &&
      !originalRequest._sessionRechecked
    ) {
      originalRequest._sessionRechecked = true;

      const stillAuthenticated = await api
        .get("/api/auth/me", { _sessionRechecked: true })
        .then(() => true)
        .catch(() => false);

      if (stillAuthenticated) {
        return api.request(originalRequest);
      }

      if (!isRedirecting) {
        isRedirecting = true;
        onUnauthorized?.();
        const currentPath = window.location.pathname;
        if (currentPath !== "/login" && currentPath !== "/register") {
          window.location.href = buildLoginPath(currentPath + window.location.search);
        }
      }
    }

    if (error.response) {
      if (error.response.status === 429) {
        const apiErrorException = new ApiErrorException({
          message:
            "Too many requests. Please wait a moment before trying again.",
          status: "TOO_MANY_REQUESTS",
          statusCode: 429,
          timestamp: new Date().toISOString(),
        });
        return Promise.reject(apiErrorException);
      }

      const responseData = error.response.data as ApiErrorResponse;

      if (
        responseData &&
        typeof responseData === "object" &&
        "statusCode" in responseData
      ) {
        const apiErrorException = new ApiErrorException(responseData);
        return Promise.reject(apiErrorException);
      }
    }

    if (error.code === "ERR_NETWORK" || error.message === "Network Error") {
      const apiErrorException = new ApiErrorException({
        message: "Network error. Please check your connection and try again.",
        status: "NETWORK_ERROR",
        statusCode: 0,
        timestamp: new Date().toISOString(),
      });
      return Promise.reject(apiErrorException);
    }

    if (error.code === "ECONNABORTED") {
      const apiErrorException = new ApiErrorException({
        message: "Request timed out. Please try again.",
        status: "TIMEOUT",
        statusCode: 0,
        timestamp: new Date().toISOString(),
      });
      return Promise.reject(apiErrorException);
    }

    return Promise.reject(error);
  },
);
