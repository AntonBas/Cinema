import { useEffect, useState } from "react";

export const useGridColumns = <T extends HTMLElement>() => {
  const [element, setElement] = useState<T | null>(null);
  const [columns, setColumns] = useState(0);

  useEffect(() => {
    if (!element) return;

    const update = () => {
      const template = getComputedStyle(element).gridTemplateColumns;
      setColumns(template ? template.split(" ").length : 0);
    };

    update();
    const observer = new ResizeObserver(update);
    observer.observe(element);

    return () => observer.disconnect();
  }, [element]);

  return { ref: setElement, columns };
};
