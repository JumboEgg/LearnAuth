// MyCarouselContainer.tsx
import React from "react";
import Carroussel from "./Carroussel";
import Card from "./Card";

interface CardItem {
  key: string;
  content: JSX.Element;
}

export default function MyCarouselContainer() {
  // 3장의 카드 정보를 준비
  const cards: CardItem[] = [
    {
      key: "1",
      content: <Card imagen="/cat.png" title="고양이의 하루" description="고양이의 하루 일상을 소개합니다." />
    },
    {
      key: "2",
      content: <Card imagen="/cat.png" title="고양이와 함께하는 시간" description="고양이와 놀아주는 방법을 배워봅시다." />
    },
    {
      key: "3",
      content: <Card imagen="/cat.png" title="고양이 건강 관리" description="고양이 건강을 위한 팁을 알아봅시다." />
    }
  ];
  return (
    <div style={{ width: "100%", textAlign: "center", marginTop: "2rem", display: "flex", justifyContent: "center",alignItems: "center"  }}>
      <Carroussel
        cards={cards}
        offset={2}         // 좌우 펼침 정도
        showArrows={false} // 화살표 표시
        width="900px" // 기본 너비
        height="300px" // 높이
        margin="0 auto"
      />
    </div>
  );
}
