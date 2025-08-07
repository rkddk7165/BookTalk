import React, { useState } from "react";
import api from "../api/axios"; // ✅ axios 인스턴스 import
import { useNavigate } from "react-router-dom";

export default function LoginPage() {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    email: "",
    password: ""
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await api.post("/login", formData); // ✅ 여기서 사용!
      console.log("로그인 성공:", res.data);
      navigate("/my-page");
    } catch (error) {
      console.error("로그인 실패:", error);
      alert("로그인 실패: " + (error.response?.data?.message || error.message));
    }
  };

  const KAKAO_CLIENT_ID = "a04c417b600ffe0107eb7063f4bfda90";
  const REDIRECT_URI = "http://localhost:5173/oauth/kakao/callback";

  const handleKakaoLogin = () => {
    const kakaoAuthUrl = `https://kauth.kakao.com/oauth/authorize?client_id=${KAKAO_CLIENT_ID}&redirect_uri=${REDIRECT_URI}&response_type=code`;
    window.location.href = kakaoAuthUrl;
};


  return (
    <div className="w-screen h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white p-10 rounded-2xl shadow-xl w-full max-w-md">
        <h2 className="text-3xl font-bold text-center text-gray-800 mb-6">
          📚 BookTalk 로그인
        </h2>
        <form onSubmit={handleSubmit} className="space-y-5 flex flex-col items-center">
          <div className="w-full max-w-[360px]">
            <label className="block text-sm text-gray-600 mb-1">이메일</label>
            <input
              type="email"
              name="email"
              placeholder="you@example.com"
              value={formData.email}
              onChange={handleChange}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <div className="w-full max-w-[360px]">
            <label className="block text-sm text-gray-600 mb-1">비밀번호</label>
            <input
              type="password"
              name="password"
              placeholder="••••••••"
              value={formData.password}
              onChange={handleChange}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <button
            type="submit"
            className="w-full max-w-[360px] bg-blue-600 text-white py-3 rounded-lg font-semibold hover:bg-blue-700 transition"
          >
            로그인
          </button>
        </form>

        {/* 🟡 카카오 로그인 버튼 자리 */}
        <div className="mt-6 flex justify-center">
          <button
            onClick={handleKakaoLogin}
            className="bg-yellow-300 hover:bg-yellow-400 text-black py-2 px-6 rounded-lg font-semibold"
          >
            카카오로 로그인
</button>

        </div>

        <p className="text-sm text-center text-gray-500 mt-5">
          아직 계정이 없으신가요?{" "}
          <a href="/join" className="text-blue-600 hover:underline">
            회원가입
          </a>
        </p>
      </div>
    </div>
  );
}
