// Carroussel.tsx
import React, { useState, useEffect } from "react";
import Carousel from "react-spring-3d-carousel";
import { config } from "react-spring";
import { JSX } from "react/jsx-runtime";

export interface CarouselCard {
  key: string;
  content: JSX.Element;
  onClick?: () => void;
}

interface CarrousselProps {
  cards: CarouselCard[];
  offset: number;
  showArrows: boolean;
  width: string;
  height: string;
  margin: string;
}

const Carroussel: React.FC<CarrousselProps> = (props) => {
  const [goToSlide, setGoToSlide] = useState<number | null>(null);
  const [offsetRadius, setOffsetRadius] = useState<number>(2);
  const [showArrows, setShowArrows] = useState<boolean>(false);

  // slides 배열에 onClick 속성을 추가 (클릭 시 해당 슬라이드로 이동)
  const table: CarouselCard[] = props.cards.map((element, index) => ({
    ...element,
    onClick: () => setGoToSlide(index)
  }));

  const [cards] = useState<CarouselCard[]>(table);

  useEffect(() => {
    setOffsetRadius(props.offset);
    setShowArrows(props.showArrows);
  }, [props.offset, props.showArrows]);

  return (
    <div
      style={{
        width: props.width,
        height: props.height,
        margin: props.margin
      }}
    >
      <Carousel
        slides={cards}
        goToSlide={goToSlide}
        offsetRadius={offsetRadius}
        showNavigation={showArrows}
        animationConfig={config.gentle}
      />
    </div>
  );
};

export default Carroussel;
