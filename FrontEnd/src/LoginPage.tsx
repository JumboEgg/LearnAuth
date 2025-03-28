import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

const LoginPage: React.FC = () => {
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  // 로그인 버튼 클릭
  const handleLogin = () => {
    // localStorage 에 저장된 회원가입 정보를 가져온다
    const registeredEmail = localStorage.getItem("registeredEmail");
    const registeredPassword = localStorage.getItem("registeredPassword");

    // 이메일/비밀번호가 맞으면 /home으로 이동
    if (email === registeredEmail && password === registeredPassword) {
      navigate("/home");
    } else {
      alert("이메일 혹은 비밀번호가 일치하지 않습니다.");
    }
  };

  return (
    <div className="h-screen w-screen flex flex-col bg-[#F5F7FC]">
      {/* 1) 상단 네비게이션 바 */}
      <nav className="flex items-center justify-between h-16 bg-white border-b border-gray-200 px-4 md:px-8">
        {/* 좌측 로고 & 메뉴 */}
        <div className="flex items-center space-x-4">
          <div className="text-xl font-bold text-blue-600">러너스</div>
          <a
            href="#"
            className="hidden md:block text-sm text-gray-700 hover:text-blue-600 transition"
          >
            About 러너스
          </a>
          <a
            href="#"
            className="hidden md:block text-sm text-gray-700 hover:text-blue-600 transition"
          >
            강의
          </a>
        </div>

        {/* 중간 검색창 */}
        <div className="flex items-center space-x-2 flex-1 justify-center max-w-md">
          <input
            type="text"
            placeholder="검색어를 입력하세요 "
            className="w-full border border-gray-300 rounded-md px-3 py-1 focus:outline-none focus:ring focus:ring-blue-200"
          />
          <button className="px-4 py-1 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition">
            검색
          </button>
        </div>

        {/* 우측 메뉴 */}
        <div className="flex items-center space-x-3">
          <button className="text-blue-600 hover:underline transition text-sm">
            회원가입 하기
          </button>
          <button className="text-gray-400 hover:text-gray-600 transition text-sm">
            로그인 하기
          </button>
        </div>
      </nav>

      {/* 2) 메인 콘텐츠 */}
      <main className="flex-1 flex items-center justify-center">
        <div className="flex flex-col items-center">
          {/* (선택) 고양이 이미지 */}
          <img
            src="/cat.png"
            alt="cat"
            className="w-32 h-32 object-contain mb-4 md:w-40 md:h-40"
          />

          {/* 로그인 폼 박스 */}
          <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md">
            <div className="mb-4">
              <label
                className="block text-sm font-medium text-gray-700"
                htmlFor="email"
              >
                아이디를 입력하세요
              </label>
              <input
                id="email"
                type="email"
                placeholder="runauth@ssafy.com"
                className="mt-1 block w-full border-gray-300 rounded-md shadow-sm p-2"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>

            <div className="mb-6">
              <label
                className="block text-sm font-medium text-gray-700"
                htmlFor="password"
              >
                비밀번호를 입력하세요
              </label>
              <input
                id="password"
                type="password"
                placeholder="***"
                className="mt-1 block w-full border-gray-300 rounded-md shadow-sm p-2"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>

            {/* 로그인 버튼 */}
            <button
              className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded transition"
              onClick={handleLogin}
            >
              로그인
            </button>

            <div className="mt-4 text-center">
              {/* 회원가입 하러 가기 링크 */}
              <a
                href="/signup"
                className="text-blue-600 hover:underline text-sm"
              >
                회원가입 하러 가기
              </a>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default LoginPage;
