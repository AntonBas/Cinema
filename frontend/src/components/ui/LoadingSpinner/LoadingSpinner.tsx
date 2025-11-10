import React from "react";
import { LoaderWrapper } from "./LoadingSpinner.styles";

interface LoadingSpinnerProps {
    text?: string;
}

const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({ text = "Loading" }) => {
    return <LoaderWrapper data-text={text} />;
};

export default LoadingSpinner;