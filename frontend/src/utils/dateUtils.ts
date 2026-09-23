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
