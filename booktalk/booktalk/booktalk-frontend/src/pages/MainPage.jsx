export default function MainPage() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-start pt-20 bg-gradient-to-r from-green-100 to-blue-200">
      <h1 className="text-4xl font-bold mb-10">Welcome to BookTalk Main Page!</h1>
      <div className="space-x-4">
        <a href="/join">
          <button className="px-6 py-3 bg-blue-500 text-white rounded-xl text-lg font-semibold hover:scale-105 transition">
            회원가입
          </button>
        </a>
        <a href="/login">
          <button className="px-6 py-3 bg-purple-500 text-white rounded-xl text-lg font-semibold hover:scale-105 transition">
            로그인
          </button>
        </a>
      </div>
    </div>
  );
}
