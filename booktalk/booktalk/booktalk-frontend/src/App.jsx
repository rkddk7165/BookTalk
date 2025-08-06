import { Routes, Route } from 'react-router-dom';
import JoinPage from './pages/JoinPage';
import MainPage from './pages/MainPage';

function App() {
  return (
    <Routes>
      <Route path="/" element={<MainPage />} />
      <Route path="/join" element={<JoinPage />} />
    </Routes>
  );
}

export default App;
