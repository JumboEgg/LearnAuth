import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

const SignupPage: React.FC = () => {
  // ----------------------------------------
  // 상태 관리
  // ----------------------------------------
  const [realName, setRealName] = useState("");
  const [nameCertified, setNameCertified] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [nickname, setNickname] = useState("");

  // React Router 의 useNavigate 훅
  const navigate = useNavigate();

  // ----------------------------------------
  // 실명 인증 로직 (예시)
  // ----------------------------------------
  const handleNameCertify = () => {
    if (realName.trim().length > 0) {
      setNameCertified(true);
    } else {
      alert("실명을 입력해주세요!");
      setNameCertified(false);
    }
  };

  // ----------------------------------------
  // 유효성 검사
  // ----------------------------------------
  const isRealNameValid = nameCertified;
  const isEmailValid = email.trim().length > 0;
  const isPasswordValid = password.trim().length > 0;
  const isConfirmValid = confirmPassword.trim().length > 0 && password === confirmPassword;
  const isNicknameValid = nickname.trim().length > 0;

  const isFormValid =
    isRealNameValid &&
    isEmailValid &&
    isPasswordValid &&
    isConfirmValid &&
    isNicknameValid;

  // ----------------------------------------
  // 회원가입 버튼 클릭
  // ----------------------------------------
  const handleSignup = () => {
    // 회원가입 정보 localStorage 에 저장
    localStorage.setItem("registeredEmail", email);
    localStorage.setItem("registeredPassword", password);

    alert(`회원가입 완료!\n이름: ${realName}\n이메일: ${email}\n닉네임: ${nickname}`);

    // 로그인 페이지로 이동
    navigate("/");
  };

  return (
    <div className="h-screen w-screen flex flex-col bg-[#F5F7FC]">
      {/* 상단 네비게이션 바 */}
      <nav className="w-full h-16 px-4 md:px-8 bg-white border-b border-gray-200 flex items-center justify-between">
        {/* 좌측 로고 & 메뉴 */}
        <div className="flex items-center space-x-6">
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

        {/* 검색창 */}
        <div className="flex items-center space-x-2 flex-1 justify-center max-w-md">
          <input
            type="text"
            placeholder="송참치"
            className="w-full border border-gray-300 rounded-md px-3 py-1 focus:outline-none focus:ring focus:ring-blue-200"
          />
          <button className="px-4 py-1 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition">
            검색
          </button>
        </div>

        {/* 우측 메뉴 */}
        <div className="flex items-center space-x-4">
          <button className="text-blue-600 hover:underline transition">
            회원가입 하기
          </button>
          <button className="text-gray-400 hover:text-gray-600 transition">
            로그인 하기
          </button>
        </div>
      </nav>

      {/* 메인 영역 */}
      <main className="flex-1 flex items-center justify-center p-4">
        <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-md">
          <h1 className="text-2xl font-bold mb-6">회원가입</h1>

          {/* 실명 입력 + 인증 버튼 */}
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              실명을 입력하세요 {isRealNameValid && <span className="text-green-500 ml-1">✔️</span>}
            </label>
            <div className="flex space-x-2">
              <input
                type="text"
                value={realName}
                onChange={(e) => setRealName(e.target.value)}
                placeholder="실명"
                className="w-full border border-gray-300 rounded-md p-2"
              />
              {nameCertified ? (
                <button
                  onClick={() => setNameCertified(false)}
                  className="bg-red-100 text-red-600 px-4 py-2 rounded-md border border-red-300"
                >
                  인증완료
                </button>
              ) : (
                <button
                  onClick={handleNameCertify}
                  className="bg-red-100 text-red-600 px-4 py-2 rounded-md border border-red-300"
                >
                  인증하기
                </button>
              )}
            </div>
          </div>

          {/* 이메일 입력 */}
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              이메일을 입력하세요 {isEmailValid && <span className="text-green-500 ml-1">✔️</span>}
            </label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="이메일"
              className="w-full border border-gray-300 rounded-md p-2"
            />
          </div>

          {/* 비밀번호 입력 */}
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              비밀번호 {isPasswordValid && <span className="text-green-500 ml-1">✔️</span>}
            </label>
            <div className="relative">
              <input
                type={showPassword ? "text" : "password"}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="비밀번호"
                className="w-full border border-gray-300 rounded-md p-2 pr-10"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-2 top-2 text-gray-400 hover:text-gray-600"
              >
                {showPassword ? "🙈" : "👁"}
              </button>
            </div>
          </div>

          {/* 비밀번호 재확인 */}
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              비밀번호 재확인 {isConfirmValid && <span className="text-green-500 ml-1">✔️</span>}
            </label>
            <div className="relative">
              <input
                type={showConfirmPassword ? "text" : "password"}
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="비밀번호 재확인"
                className="w-full border border-gray-300 rounded-md p-2 pr-10"
              />
              <button
                type="button"
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                className="absolute right-2 top-2 text-gray-400 hover:text-gray-600"
              >
                {showConfirmPassword ? "🙈" : "👁"}
              </button>
            </div>
          </div>

          {/* 닉네임 입력 */}
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              닉네임 {nickname.trim().length > 0 && <span className="text-green-500 ml-1">✔️</span>}
            </label>
            <input
              type="text"
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              placeholder="닉네임"
              className="w-full border border-gray-300 rounded-md p-2"
            />
          </div>

          {/* 회원가입 버튼 */}
          <button
            disabled={!isFormValid}
            onClick={handleSignup}
            className={`w-full py-2 rounded-md text-white font-bold transition ${
              isFormValid
                ? "bg-blue-600 hover:bg-blue-700"
                : "bg-blue-200 cursor-not-allowed"
            }`}
          >
            회원가입
          </button>
        </div>
      </main>
    </div>
  );
};

export default SignupPage;
