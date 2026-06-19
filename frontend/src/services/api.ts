import axios from "axios";
import { ApiErrorException } from "@/utils/apiErrorHandler";

interface ApiErrorResponse {
  status: string;
  statusCode: number;
  message: string;
  timestamp: string;
  errors?: string[];
}

let isRefreshing = false;
let isRedirecting = false;
let failedQueue: Array<{
  resolve: (value: unknown) => void;
  reject: (reason?: unknown) => void;
}> = [];

const processQueue = (error: Error | null, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080",
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 10000,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("authToken");

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (
      originalRequest.url?.includes("/api/auth/login") ||
      originalRequest.url?.includes("/api/auth/register")
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

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return api(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const response = await axios.post(
          `${import.meta.env.VITE_API_URL || "http://localhost:8080"}/api/auth/refresh`,
          {},
          { withCredentials: true },
        );

        const newToken = response.data.accessToken;
        localStorage.setItem("authToken", newToken);
        api.defaults.headers.common.Authorization = `Bearer ${newToken}`;
        originalRequest.headers.Authorization = `Bearer ${newToken}`;

        processQueue(null, newToken);
        return api(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError as Error, null);
        localStorage.removeItem("authToken");

        if (!isRedirecting) {
          isRedirecting = true;
          const currentPath = window.location.pathname;
          if (currentPath !== "/login" && currentPath !== "/register") {
            window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`;
          }
        }

        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
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

    if (error.code === "NETWORK_ERROR" || error.message === "Network Error") {
      console.error("Network error - please check your connection");
    }

    if (error.code === "ECONNABORTED") {
      console.error("Request timeout - please try again");
    }

    return Promise.reject(error);
  },
);

export const setAuthToken = (token: string | null) => {
  if (token) {
    localStorage.setItem("authToken", token);
  } else {
    localStorage.removeItem("authToken");
  }
};

export const getAuthToken = (): string | null => {
  return localStorage.getItem("authToken");
};
