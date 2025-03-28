import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";

import MainPage from "./MainPage";      // 새로 만든 마이페이지 컴포넌트 임포트
import LoginPage from "./LoginPage";
import SignupPage from "./SignupPage";
import "./index.css"; // Tailwind

const App: React.FC = () => {
  return (
    <Router>
      <Routes>
        {/* 첫 화면("/") -> 마이페이지 */}
        <Route path="/" element={<MainPage />} />

        {/* /login -> 로그인 페이지 */}
        <Route path="/login" element={<LoginPage />} />

        {/* /signup -> 회원가입 페이지 */}
        <Route path="/signup" element={<SignupPage />} />
      </Routes>
    </Router>
  );
};

export default App;
