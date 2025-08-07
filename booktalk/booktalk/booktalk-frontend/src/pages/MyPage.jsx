import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios"; // withCredentials 포함된 인스턴스

export default function MyPage() {
  const [message, setMessage] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    api.get("/myPage")
      .then((res) => {
        setMessage(res.data);
      })
      .catch((err) => {
        console.error(err);
        setMessage("접근 권한이 없습니다.");
      });
  }, []);

  const handleLogout = async () => {
    try {
      await api.post("/logout");
      localStorage.clear(); // 혹시 로그인 정보 저장했으면 제거
      navigate("/"); // 메인페이지로 이동
    } catch (err) {
      console.error("로그아웃 실패:", err);
      alert("로그아웃 실패");
    }
  };

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-100">
      <h1 className="text-2xl font-semibold mb-6">{message}</h1>
      <button
        onClick={handleLogout}
        className="bg-red-500 text-white px-6 py-2 rounded-lg hover:bg-red-600 transition"
      >
        로그아웃
      </button>
    </div>
  );
}