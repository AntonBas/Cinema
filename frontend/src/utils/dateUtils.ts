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
