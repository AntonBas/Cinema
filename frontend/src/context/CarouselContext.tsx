import React, { createContext, useContext, useState, useEffect, useRef } from 'react';

interface CarouselContextType {
    currentIndex: number;
    nextSlide: () => void;
    prevSlide: () => void;
    goToSlide: (index: number) => void;
    isHovered: boolean;
    setIsHovered: (hovered: boolean) => void;
}

const CarouselContext = createContext<CarouselContextType | null>(null);

export const CarouselProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [currentIndex, setCurrentIndex] = useState(0);
    const [isHovered, setIsHovered] = useState(false);
    const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);
    const maxIndex = 3;

    const nextSlide = () => {
        setCurrentIndex((prev) => (prev >= maxIndex ? 0 : prev + 1));
    };

    const prevSlide = () => {
        setCurrentIndex((prev) => (prev <= 0 ? maxIndex : prev - 1));
    };

    const goToSlide = (index: number) => {
        setCurrentIndex(Math.min(Math.max(0, index), maxIndex));
    };

    useEffect(() => {
        if (isHovered) return;

        timerRef.current = setInterval(() => {
            nextSlide();
        }, 5000);

        return () => {
            if (timerRef.current) {
                clearInterval(timerRef.current);
            }
        };
    }, [isHovered]);

    return (
        <CarouselContext.Provider value={{ currentIndex, nextSlide, prevSlide, goToSlide, isHovered, setIsHovered }}>
            {children}
        </CarouselContext.Provider>
    );
};

export const useCarousel = () => {
    const context = useContext(CarouselContext);
    if (!context) {
        throw new Error('useCarousel must be used within CarouselProvider');
    }
    return context;
};