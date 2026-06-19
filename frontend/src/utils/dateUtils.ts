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
