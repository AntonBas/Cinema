import { afterEach, describe, expect, it, vi } from "vitest";
import {
  addDaysToIsoDate,
  formatTimeLeft,
  getCinemaDateTimeLocal,
  getCinemaToday,
  getMinutesUntil,
  parseServerInstant,
  toBackendFormat,
} from "./dateUtils";

const NOW = Date.parse("2026-01-01T12:00:00Z");
const minutesFromNow = (minutes: number) =>
  new Date(NOW + minutes * 60_000).toISOString();

describe("toBackendFormat", () => {
  it.each([
    ["25.12.2024", "2024-12-25"],
    ["2024-12-25", "2024-12-25"],
    ["12/25/2024", ""],
    [null, ""],
  ])("converts %s to %s", (value, expected) => {
    expect(toBackendFormat(value)).toBe(expected);
  });
});

describe("parseServerInstant", () => {
  it("treats a value without zone as UTC", () => {
    expect(parseServerInstant("2026-01-01T10:00:00").toISOString()).toBe(
      "2026-01-01T10:00:00.000Z",
    );
  });

  it("respects an explicit offset", () => {
    expect(parseServerInstant("2026-01-01T12:00:00+02:00").toISOString()).toBe(
      "2026-01-01T10:00:00.000Z",
    );
  });
});

describe("addDaysToIsoDate", () => {
  it("handles leap years and month boundaries", () => {
    expect(addDaysToIsoDate("2024-02-28", 1)).toBe("2024-02-29");
    expect(addDaysToIsoDate("2024-12-31", 1)).toBe("2025-01-01");
    expect(addDaysToIsoDate("2024-03-01", -1)).toBe("2024-02-29");
  });
});

describe("cinema clock", () => {
  afterEach(() => {
    vi.useRealTimers();
  });

  it("uses the Kyiv date and time", () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2026-01-01T23:30:00Z"));

    expect(getCinemaToday()).toBe("2026-01-02");
    expect(getCinemaDateTimeLocal()).toBe("2026-01-02T01:30");
    expect(getCinemaDateTimeLocal(30)).toBe("2026-01-02T02:00");
  });
});

describe("booking time left", () => {
  it("counts whole minutes until expiry", () => {
    expect(getMinutesUntil(minutesFromNow(19.9), NOW)).toBe(19);
    expect(getMinutesUntil(minutesFromNow(-5), NOW)).toBe(0);
  });

  it.each([
    [90, "1h 30m"],
    [60, "1h 0m"],
    [15, "15m"],
    [0.5, "0m"],
  ])("shows %s minutes as %s", (minutes, expected) => {
    expect(formatTimeLeft(minutesFromNow(minutes), NOW)).toBe(expected);
  });

  it("shows Expired once the deadline has passed", () => {
    expect(formatTimeLeft(minutesFromNow(0), NOW)).toBe("Expired");
    expect(formatTimeLeft(minutesFromNow(-1), NOW)).toBe("Expired");
  });
});
