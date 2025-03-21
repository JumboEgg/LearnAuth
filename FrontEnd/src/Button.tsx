// Button.tsx
import React from "react";
import Styles from "./Button.module.css";

interface ButtonProps {
  text: string;
}

const Button: React.FC<ButtonProps> = ({ text }) => {
  return <button className={Styles.btn}>{text}</button>;
};

export default Button;
