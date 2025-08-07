import { Routes, Route } from 'react-router-dom';
import JoinPage from './pages/JoinPage';
import MainPage from './pages/MainPage';
import LoginPage from "./pages/LoginPage";
import MyPage from "./pages/MyPage";
import KakaoRedirectPage from "./pages/KakaoRedirectPage";

function App() {
  return (
    <Routes>
      <Route path="/" element={<MainPage />} />
      <Route path="/join" element={<JoinPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/my-page" element={<MyPage />} />
      <Route path="/oauth/kakao/callback" element={<KakaoRedirectPage />} />
    </Routes>
  );
}

export default App;
