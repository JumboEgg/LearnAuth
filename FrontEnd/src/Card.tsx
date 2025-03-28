// Card.tsx
import Styles from "./Card.module.css";
import React, { useState } from "react";
import { useSpring, animated } from "react-spring";
import Button from "./Button";

interface CardProps {
  imagen: string;
  title: string;
  description: string;
}

const Card: React.FC<CardProps> = ({ imagen, title, description }) => {
  const [show, setShown] = useState(false);

  // 마우스 올렸을 때 살짝 확대 & 그림자
  const props3 = useSpring({
    transform: show ? "scale(1.03)" : "scale(1)",
    boxShadow: show
      ? "0 20px 25px rgb(0 0 0 / 25%)"
      : "0 2px 10px rgb(0 0 0 / 8%)"
  });

  return (
    <animated.div
      className={Styles.card}
      style={props3}
      onMouseEnter={() => setShown(true)}
      onMouseLeave={() => setShown(false)}
    >
      <img src={imagen} alt={title} />
      <h2>{title}</h2>
      <p>{description}</p>
      <div className={Styles.btnn}>
        <Button text="Demo" />
        <Button text="Code" />
      </div>
    </animated.div>
  );
};

export default Card;
