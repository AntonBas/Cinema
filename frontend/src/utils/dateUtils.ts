export const toBackendFormat = (
  dateString: string | null | undefined,
): string => {
  if (!dateString) return "";

  try {
    if (/^\d{4}-\d{2}-\d{2}$/.test(dateString)) {
      return dateString;
    }

    if (/^\d{2}\.\d{2}\.\d{4}$/.test(dateString)) {
      const [day, month, year] = dateString.split(".");
      return `${year}-${month}-${day}`;
    }

    return "";
  } catch (error) {
    console.error("Date conversion error:", error);
    return "";
  }
};

export const toDisplayFormat = (
  dateString: string | null | undefined,
): string => {
  if (!dateString) return "—";

  try {
    if (/^\d{4}-\d{2}-\d{2}$/.test(dateString)) {
      const [year, month, day] = dateString.split("-");
      return `${day}.${month}.${year}`;
    }

    return dateString;
  } catch (error) {
    console.error("Date conversion error:", error);
    return "—";
  }
};

export const safeFormatDate = (
  dateString: string | null | undefined,
): string => {
  if (!dateString) return "—";
  return toDisplayFormat(dateString);
};

export const formatInstantDate = (
  instantString: string | null | undefined,
): string => {
  if (!instantString) return "—";
  const date = parseServerInstant(instantString);
  if (isNaN(date.getTime())) return "—";
  const day = String(date.getDate()).padStart(2, "0");
  const month = String(date.getMonth() + 1).padStart(2, "0");
  return `${day}.${month}.${date.getFullYear()}`;
};

export const parseServerInstant = (dateTimeString: string): Date => {
  const hasTimezoneDesignator = /(Z|[+-]\d{2}:?\d{2})$/.test(dateTimeString);
  return new Date(
    hasTimezoneDesignator ? dateTimeString : `${dateTimeString}Z`,
  );
};
