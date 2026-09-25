import { useEffect, useState } from "react";

export const useSettledLoad = (load: () => Promise<unknown>) => {
  const [settled, setSettled] = useState(false);

  useEffect(() => {
    let active = true;
    setSettled(false);
    load()
      .catch(() => undefined)
      .finally(() => {
        if (active) setSettled(true);
      });
    return () => {
      active = false;
    };
  }, [load]);

  return settled;
};
