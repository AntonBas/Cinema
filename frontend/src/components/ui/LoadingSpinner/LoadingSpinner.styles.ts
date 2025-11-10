import styled, { keyframes } from "styled-components";

const motion = keyframes`
  0%, 50%, 100% {
    transform: translateX(0) scale(1);
  }
  25% {
    transform: translateX(-90px) scale(0.7);
  }
  75% {
    transform: translateX(90px) scale(0.7);
  }
`;

const textShine = keyframes`
  0% {
    background-position: -200% center;
  }
  100% {
    background-position: 200% center;
  }
`;

export const LoaderWrapper = styled.span`
  position: relative;
  font-size: 48px;
  letter-spacing: 2px;
  display: inline-block;
  font-weight: 700;
  font-family: 'Arial', sans-serif;

  &::before {
    content: attr(data-text);
    background: linear-gradient(
      90deg,
      #ffffff,
      #fd5f00 30%,
      #ff8c00 50%,
      #fd5f00 70%,
      #ffffff
    );
    background-size: 200% auto;
    background-clip: text;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    animation: ${textShine} 3s ease-in-out infinite;
    display: block;
  }

  &::after {
    content: "🍿";
    position: absolute;
    inset: 0;
    margin: auto;
    top: -70px;
    left: 0;
    width: 30px;
    height: 30px;
    font-size: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    animation: ${motion} 2.8s ease-in-out infinite;
  }

  /* Адаптація для різних розмірів */
  @media (max-width: 768px) {
    font-size: 36px;
    
    &::after {
      font-size: 32px;
      top: -60px;
    }
  }
`;

// Додатковий контейнер для центрування
export const LoaderContainer = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
  padding: 20px;
`;