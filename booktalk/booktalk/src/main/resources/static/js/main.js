function toggleTheme() {
    const body = document.body;
    if (body.classList.contains('light')) {
        body.classList.remove('light');
        body.classList.add('dark');
        localStorage.setItem('theme', 'dark');
    } else {
        body.classList.remove('dark');
        body.classList.add('light');
        localStorage.setItem('theme', 'light');
    }
}

// 초기 진입 시 로컬스토리지 값 적용
document.addEventListener("DOMContentLoaded", () => {
    const saved = localStorage.getItem('theme') || 'light';
    document.body.classList.add(saved);
});
