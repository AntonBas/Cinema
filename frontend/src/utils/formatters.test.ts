import { describe, expect, it } from "vitest";
import {
  formatDate,
  formatDateRange,
  formatDateTime,
  formatPhoneNumber,
  formatPrice,
  formatTime,
} from "./formatters";

describe("formatPrice", () => {
  it.each([
    [150, "150.00 ₴"],
    ["99.5", "99.50 ₴"],
    [0, "0.00 ₴"],
  ])("formats %s as %s", (value, expected) => {
    expect(formatPrice(value)).toBe(expected);
  });

  it.each([null, undefined, "abc"])("shows a dash for %s", (value) => {
    expect(formatPrice(value)).toBe("—");
  });
});

describe("formatPhoneNumber", () => {
  it("formats Ukrainian numbers", () => {
    expect(formatPhoneNumber("+380671234567")).toBe("+380 (67) 123-45-67");
    expect(formatPhoneNumber("380671234567")).toBe("+380 (67) 123-45-67");
  });

  it("keeps unknown formats as they are", () => {
    expect(formatPhoneNumber("12345")).toBe("12345");
  });

  it("shows a dash for an empty value", () => {
    expect(formatPhoneNumber(null)).toBe("—");
  });
});

describe("date formatting", () => {
  it("shows server moments in cinema time", () => {
    expect(formatTime("2026-03-10T16:30:00Z")).toBe("18:30");
    expect(formatTime("2026-07-10T15:30:00Z")).toBe("18:30");
  });

  it("shows local date-times as they are", () => {
    expect(formatTime("2026-03-10T18:30:00")).toBe("18:30");
    expect(formatDateTime("2026-03-10T18:30:00")).toBe("Mar 10, 2026, 18:30");
  });

  it("formats date-only values without shifting the day", () => {
    expect(formatDate("2026-03-10")).toBe("Mar 10, 2026");
  });

  it("shows a dash for missing or invalid dates", () => {
    expect(formatDate(null)).toBe("—");
    expect(formatDate("not-a-date")).toBe("—");
  });

  it("omits the year for a range within the current year", () => {
    const year = new Date().getFullYear();

    expect(formatDateRange(`${year}-01-05`, `${year}-01-10`)).toBe(
      "Jan 5 – Jan 10",
    );
  });

  it("keeps the year for a range outside the current year", () => {
    expect(formatDateRange("2024-12-30", "2025-01-02")).toBe(
      "Dec 30, 2024 – Jan 2, 2025",
    );
  });
});
