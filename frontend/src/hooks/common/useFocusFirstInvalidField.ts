import { useCallback, useEffect, useRef, useState } from "react";

export const useFocusFirstInvalidField = () => {
  const formRef = useRef<HTMLFormElement>(null);
  const [invalidSubmitCount, setInvalidSubmitCount] = useState(0);

  useEffect(() => {
    if (invalidSubmitCount === 0) return;
    const field = formRef.current?.querySelector<HTMLElement>(
      '[aria-invalid="true"]',
    );
    field?.scrollIntoView({ behavior: "smooth", block: "center" });
    field?.focus({ preventScroll: true });
  }, [invalidSubmitCount]);

  const focusFirstInvalidField = useCallback(
    () => setInvalidSubmitCount((count) => count + 1),
    [],
  );

  return { formRef, focusFirstInvalidField };
};
