import { describe, expect, it } from "vitest";
import type { ApiError } from "@/types/api";
import { ApiErrorException, isApiErrorException } from "./apiErrorHandler";

const apiError = (overrides: Partial<ApiError> = {}): ApiError => ({
  timestamp: "15-10-2026 12:00:00",
  status: "BAD_REQUEST",
  statusCode: 400,
  message: "Validation failed",
  ...overrides,
});

describe("ApiErrorException", () => {
  it("uses the backend message and status code", () => {
    const error = new ApiErrorException(
      apiError({ status: "NOT_FOUND", statusCode: 404, message: "Not found" }),
    );

    expect(error.message).toBe("Not found");
    expect(error.statusCode).toBe(404);
    expect(error.isNotFound()).toBe(true);
    expect(error.isClientError()).toBe(true);
    expect(error.toString()).toBe("[404] Not found");
  });

  it("replaces a generic 429 message with a rate limit hint", () => {
    const error = new ApiErrorException(
      apiError({ statusCode: 429, message: "Request failed" }),
    );

    expect(error.message).toBe(
      "Too many requests. Please wait a moment before trying again.",
    );
    expect(error.isTooManyRequests()).toBe(true);
  });

  it("joins unique sub-error messages into the error message", () => {
    const error = new ApiErrorException(
      apiError({
        subErrors: [
          { field: "email", message: "Invalid email" },
          { field: "user.email", message: "Invalid email" },
          { field: "phoneNumber", message: "Invalid phone number" },
          { field: "city" },
        ],
      }),
    );

    expect(error.message).toBe("Invalid email; Invalid phone number");
    expect(error.isValidationError()).toBe(true);
  });

  it("maps sub-errors to their last field segment", () => {
    const error = new ApiErrorException(
      apiError({
        subErrors: [
          { field: "user.address.city", message: "City is required" },
          { field: "seats[0].row", message: "Row is invalid" },
          { message: "No field" },
        ],
      }),
    );

    expect(error.getValidationErrors()).toEqual({
      city: "City is required",
      row: "Row is invalid",
    });
    expect(error.getFirstValidationError()).toBe("City is required");
  });

  it("returns no validation errors when there are no sub-errors", () => {
    const error = new ApiErrorException(apiError());

    expect(error.isValidationError()).toBe(false);
    expect(error.getValidationErrors()).toEqual({});
    expect(error.getFirstValidationError()).toBeNull();
  });

  it.each([
    ["CONFLICT", 409],
    ["UNAUTHORIZED", 401],
    ["FORBIDDEN", 403],
    ["418", 418],
    ["SOMETHING_ELSE", 500],
  ])("derives status code from status %s", (status, expected) => {
    const error = new ApiErrorException(apiError({ status, statusCode: 0 }));

    expect(error.statusCode).toBe(expected);
  });

  it("falls back to defaults for an empty payload", () => {
    const error = new ApiErrorException({} as ApiError);

    expect(error.message).toBe("Request failed");
    expect(error.status).toBe("UNKNOWN_ERROR");
    expect(error.statusCode).toBe(500);
    expect(error.isServerError()).toBe(true);
  });

  it("is recognised by the type guard", () => {
    expect(isApiErrorException(new ApiErrorException(apiError()))).toBe(true);
    expect(isApiErrorException(new Error("plain"))).toBe(false);
  });
});
