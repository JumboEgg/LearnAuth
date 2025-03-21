import React, { useState, useEffect } from "react";
import "./index.css";
import MyCarouselContainer from "./MyCarouselContainer";

const MainPage: React.FC = () => {
  // 데모용 강의 데이터
  const dummyItems = [
    {
      title: "눈 감고 차이는 법",
      price: "10,000 CAT",
      category: "데이터",
      thumbnail: "cat.png",
    },
    {
      title: "정보처리기사(필기) 특강",
      price: "999,999,999 CAT",
      category: "데이터",
      thumbnail: "cat.png",
    },
    {
      title: "뇌를 깨우는 10가지 방법",
      price: "10,000 CAT",
      category: "체육",
      thumbnail: "cat.png",
    },
    {
      title: "2025년 저기압을 피하는 법",
      price: "10,000 CAT",
      category: "생명과학",
      thumbnail: "cat.png",
    },
  ];
  const categories = ["데이터", "체육", "생명과학"];

  return (
    <div className="w-screen min-h-screen flex flex-col bg-gray-50 overflow-auto">
      {/* 상단 네비게이션 */}
      <header className="bg-white px-4 py-3 border-b border-gray-200 flex items-center justify-between">
        {/* 좌측 로고 */}
        <div className="flex items-center space-x-6">
          <div className="text-2xl font-bold text-blue-600">러너스</div>
          <nav className="hidden md:flex space-x-4">
            <a href="#" className="text-gray-700 hover:text-blue-600">
              About 러너스
            </a>
            <a href="#" className="text-gray-700 hover:text-blue-600">
              강의
            </a>
          </nav>
        </div>

        {/* 중앙 검색창 */}
        <div className="flex-1 mx-8 max-w-lg relative hidden md:block">
          <input
            type="text"
            placeholder="입력"
            className="w-full border border-gray-300 rounded-full px-4 py-2 pr-10 focus:outline-none focus:ring-2 focus:ring-blue-200"
          />
          <button className="absolute right-2 top-1/2 transform -translate-y-1/2 bg-blue-600 text-white px-4 py-1 rounded-full hover:bg-blue-700">
            검색
          </button>
        </div>

        {/* 우측 메뉴 */}
        <div className="flex items-center space-x-4">
          <button className="bg-blue-100 text-blue-600 px-3 py-1 rounded-full text-sm hover:bg-blue-200">
            + 강의 등록하기
          </button>
          <button
            onClick={() => (window.location.href = "login")}
            className="text-sm text-gray-400 hover:text-gray-700"
          >
            로그인 하러 가기
          </button>
        </div>
      </header>

      {/* 
        인기 강의 캐러셀 
        (배너 아래, 사진 예시처럼 "현재 가장 인기 있는 강의" 섹션)
      */}
      <section className="p-4 md:p-8">
        <h2 className="text-xl font-bold mb-3">현재 가장 인기 있는 강의</h2>
              <MyCarouselContainer />
      </section>

      {/* 아래쪽 카테고리 목록 (스크롤) */}
      <main className="flex-1 p-4 md:p-8 space-y-8 overflow-auto">
        {categories.map((cat) => {
          const filtered = dummyItems.filter((it) => it.category === cat);
          return (
            <section key={cat}>
              <h3 className="text-xl font-semibold mb-3">{cat}</h3>
              <div className="relative">
                <div className="overflow-x-auto whitespace-nowrap space-x-4 scrollbar-hide">
                  {filtered.length > 0 ? (
                    filtered.map((item, idx) => (
                      <div
                        key={idx}
                        className="inline-block align-top w-40 bg-white shadow-md rounded-md mr-4 overflow-hidden"
                      >
                        <img
                          src={item.thumbnail}
                          alt={item.title}
                          className="w-full h-24 object-cover"
                        />
                        <div className="p-3">
                          <div className="text-sm font-medium mb-1">
                            {item.title}
                          </div>
                          <div className="text-xs text-gray-600">
                            {item.price}
                          </div>
                        </div>
                      </div>
                    ))
                  ) : (
                    <div className="inline-block text-gray-400">
                      강의가 없습니다.
                    </div>
                  )}
                </div>
              </div>
            </section>
          );
        })}
      </main>
    </div>
  );
};

export default MainPage;