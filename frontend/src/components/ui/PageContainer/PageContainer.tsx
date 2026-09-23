import React from "react";
import clsx from "clsx";
import styles from "./PageContainer.module.css";

export type PageContainerSize = "narrow" | "default" | "wide";

export interface PageContainerProps {
  children: React.ReactNode;
  size?: PageContainerSize;
  className?: string;
}

export const PageContainer: React.FC<PageContainerProps> = ({
  children,
  size = "default",
  className = "",
}) => (
  <div className={clsx(styles.container, styles[size], className)}>
    {children}
  </div>
);
