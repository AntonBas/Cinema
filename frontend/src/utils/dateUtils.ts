const ISO_DATE = /^\d{4}-\d{2}-\d{2}$/;
const DISPLAY_DATE = /^\d{2}\.\d{2}\.\d{4}$/;
const TIME_ZONE_DESIGNATOR = /(Z|[+-]\d{2}:?\d{2})$/;

export const toBackendFormat = (
  dateString: string | null | undefined,
): string => {
  if (!dateString) return "";
  if (ISO_DATE.test(dateString)) return dateString;
  if (DISPLAY_DATE.test(dateString)) {
    const [day, month, year] = dateString.split(".");
    return `${year}-${month}-${day}`;
  }
  return "";
};

export const parseServerInstant = (dateTimeString: string): Date =>
  new Date(
    TIME_ZONE_DESIGNATOR.test(dateTimeString)
      ? dateTimeString
      : `${dateTimeString}Z`,
  );

const MILLIS_PER_MINUTE = 60_000;

export const getMinutesUntil = (
  dateTimeString: string,
  now = Date.now(),
): number =>
  Math.max(
    0,
    Math.floor(
      (parseServerInstant(dateTimeString).getTime() - now) / MILLIS_PER_MINUTE,
    ),
  );

export const formatTimeLeft = (
  dateTimeString: string,
  now = Date.now(),
): string => {
  if (parseServerInstant(dateTimeString).getTime() <= now) return "Expired";
  const minutes = getMinutesUntil(dateTimeString, now);
  const hours = Math.floor(minutes / 60);
  return hours > 0 ? `${hours}h ${minutes % 60}m` : `${minutes}m`;
};

const CINEMA_TIME_ZONE = "Europe/Kyiv";

const cinemaDateFormatter = new Intl.DateTimeFormat("en-CA", {
  timeZone: CINEMA_TIME_ZONE,
  year: "numeric",
  month: "2-digit",
  day: "2-digit",
});

export const getCinemaToday = (): string =>
  cinemaDateFormatter.format(new Date());

export const addDaysToIsoDate = (isoDate: string, days: number): string => {
  const date = new Date(`${isoDate}T00:00:00Z`);
  date.setUTCDate(date.getUTCDate() + days);
  return date.toISOString().split("T")[0];
};

const cinemaDateTimeFormatter = new Intl.DateTimeFormat("sv-SE", {
  timeZone: CINEMA_TIME_ZONE,
  year: "numeric",
  month: "2-digit",
  day: "2-digit",
  hour: "2-digit",
  minute: "2-digit",
  hourCycle: "h23",
});

export const getCinemaDateTimeLocal = (offsetMinutes = 0): string =>
  cinemaDateTimeFormatter
    .format(new Date(Date.now() + offsetMinutes * 60_000))
    .replace(" ", "T");
