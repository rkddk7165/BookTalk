import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios"; // withCredentials 포함된 인스턴스

export default function KakaoRedirectPage() {
  const navigate = useNavigate();

  useEffect(() => {
    const code = new URL(window.location.href).searchParams.get("code");

    if (!code) {
      alert("인가 코드가 없습니다.");
      navigate("/login");
      return;
    }

    api.post("/users/oauth/kakao", { code })
      .then((res) => {
        console.log("카카오 로그인 성공:", res.data);
        navigate("/my-page");
      })
      .catch((err) => {
        console.error("카카오 로그인 실패:", err);
        alert("카카오 로그인 실패: " + (err.response?.data?.message || err.message));
        navigate("/login");
      });
  }, []);

  return <p className="text-center mt-10">카카오 로그인 처리 중...</p>;
}
