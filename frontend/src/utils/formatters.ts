const DISPLAY_LOCALE = "en-US";
const CINEMA_TIME_ZONE = "Europe/Kyiv";
const TIME_ZONE_DESIGNATOR = /(Z|[+-]\d{2}:?\d{2})$/;
const DATE_ONLY = /^\d{4}-\d{2}-\d{2}$/;
const EMPTY_VALUE = "—";

type DateInput = string | null | undefined;

interface ZonedDate {
  date: Date;
  timeZone: string;
}

const toZonedDate = (value: DateInput): ZonedDate | null => {
  if (!value) return null;
  const isMoment = TIME_ZONE_DESIGNATOR.test(value);
  const isoValue = isMoment
    ? value
    : DATE_ONLY.test(value)
      ? `${value}T00:00:00Z`
      : `${value}Z`;
  const date = new Date(isoValue);
  if (isNaN(date.getTime())) return null;
  return { date, timeZone: isMoment ? CINEMA_TIME_ZONE : "UTC" };
};

const format = (
  value: DateInput,
  options: Intl.DateTimeFormatOptions,
): string => {
  const zoned = toZonedDate(value);
  if (!zoned) return EMPTY_VALUE;
  return new Intl.DateTimeFormat(DISPLAY_LOCALE, {
    ...options,
    timeZone: zoned.timeZone,
  }).format(zoned.date);
};

const TIME_OPTIONS: Intl.DateTimeFormatOptions = {
  hour: "2-digit",
  minute: "2-digit",
  hourCycle: "h23",
};

export const formatDate = (value: DateInput): string =>
  format(value, { year: "numeric", month: "short", day: "numeric" });

export const formatShortDate = (value: DateInput): string =>
  format(value, { month: "short", day: "numeric" });

export const formatWeekdayDate = (value: DateInput): string =>
  format(value, { weekday: "short", month: "short", day: "numeric" });

export const formatFullDate = (value: DateInput): string =>
  format(value, {
    weekday: "short",
    year: "numeric",
    month: "short",
    day: "numeric",
  });

export const formatTime = (value: DateInput, withSeconds = false): string =>
  format(
    value,
    withSeconds ? { ...TIME_OPTIONS, second: "2-digit" } : TIME_OPTIONS,
  );

export const formatDateTime = (value: DateInput): string =>
  format(value, {
    year: "numeric",
    month: "short",
    day: "numeric",
    ...TIME_OPTIONS,
  });

export const formatFullDateTime = (value: DateInput): string =>
  format(value, {
    weekday: "short",
    year: "numeric",
    month: "short",
    day: "numeric",
    ...TIME_OPTIONS,
  });

export const formatPrice = (
  value: number | string | null | undefined,
): string => {
  const amount = typeof value === "string" ? parseFloat(value) : value;
  if (amount == null || isNaN(amount)) return EMPTY_VALUE;
  return `${amount.toFixed(2)} ₴`;
};
