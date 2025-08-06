import { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

export default function JoinPage() {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    email: "",
    nickname: "",
    password: "",
    profileImage: "",
    host: "LOCAL",
    snsId: ""
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await axios.post("http://localhost:8080/users", formData);
      console.log("회원가입 성공:", res.data);
      navigate("/");
    } catch (error) {
      console.error("회원가입 실패:", error);
      alert("회원가입 실패: " + (error.response?.data?.message || error.message));
    }
  };

 return (
    <div className="min-h-screen flex justify-center items-center bg-gray-900 text-white">
      <div className="w-[400px] bg-gray-800 p-8 rounded-lg shadow-lg translate-y-[-100px]">
        <h1 className="text-3xl font-bold mb-6 text-center">📚 BookTalk 회원가입</h1>
        <form onSubmit={handleSubmit} className="space-y-4">
          <input
            type="email"
            name="email"
            placeholder="이메일"
            value={formData.email}
            onChange={handleChange}
            className="w-full h-12 px-4 rounded-md bg-gray-700 border border-gray-600 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <input
            type="text"
            name="nickname"
            placeholder="닉네임"
            value={formData.nickname}
            onChange={handleChange}
            className="w-full h-12 px-4 rounded-md bg-gray-700 border border-gray-600 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <input
            type="password"
            name="password"
            placeholder="비밀번호"
            value={formData.password}
            onChange={handleChange}
            className="w-full h-12 px-4 rounded-md bg-gray-700 border border-gray-600 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <input
            type="text"
            name="profileImageUrl"
            placeholder="프로필 이미지 URL"
            value={formData.profileImageUrl}
            onChange={handleChange}
            className="w-full h-12 px-4 rounded-md bg-gray-700 border border-gray-600 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <select
            name="host"
            value={formData.host}
            onChange={handleChange}
            className="w-full h-12 px-4 rounded-md bg-gray-700 border border-gray-600"
          >
            <option value="LOCAL">LOCAL</option>
            <option value="KAKAO">KAKAO</option>
          </select>
          <input
            type="text"
            name="snsId"
            placeholder="SNS ID"
            value={formData.snsId}
            onChange={handleChange}
            className="w-full h-12 px-4 rounded-md bg-gray-700 border border-gray-600 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <button
            type="submit"
            className="w-full h-12 bg-blue-600 hover:bg-blue-700 rounded-md font-semibold"
          >
            가입하기
          </button>
        </form>
      </div>
    </div>
  );
}
