// src/main/java/myproject/booktalk/init/InitData.java
package myproject.booktalk.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import myproject.booktalk.board.Board;
import myproject.booktalk.board.repository.BoardRepository;
import myproject.booktalk.post.Post;
import myproject.booktalk.post.PostRepository;
import myproject.booktalk.user.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
//@Profile("local")        // 로컬에서만
@Order(2)                // ← 보드 생성 Runner(1) 다음
@RequiredArgsConstructor
public class InitData implements ApplicationRunner {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 1) 유저 upsert
        User user = upsert("rkddk7165@naver.com", "민락주점", Role.USER);
        User admin = upsert("admin@naver.com", "관리자", Role.ADMIN);
        User user3 = upsert("1111@naver.com", "카이사", Role.USER);
        user3.setProfileImage("/uploads/profile/u3_5618c486-86fd-4336-8874-0088e24f41d9.jfif");

        // 2) 게시판 조회 (없으면 스킵하고 경고만 찍음)
        Board free = boardRepository.findByTitle("자유게시판")
                .orElse(null);
        Board recommend = boardRepository.findByTitle("책추천게시판")
                .orElse(null);
        Board quotes = boardRepository.findByTitle("한줄글귀게시판")
                .orElse(null);

        if (free == null || recommend == null || quotes == null) {
            log.warn("고정 게시판이 아직 생성되지 않았습니다. BoardInitializer가 먼저 실행되도록 @Order를 확인하세요.");
            return; // 보드 없으면 데이터 주입 스킵
        }

        // 이미 글이 있으면 재주입 방지
        if (postRepository.existsByBoardId(free.getId())) return;

        // 3) user3가 작성자로 샘플 글 주입
        User writer = user3;

        // 자유: 공지 2 + 일반 6
        postRepository.saveAll(List.of(
                makePost(free, writer, "[공지] 커뮤니티 이용 안내", "자유게시판 사용 규칙을 꼭 읽어주세요.", true),
                makePost(free, writer, "[공지] 업데이트 안내", "새로운 기능이 추가되었습니다.", true),
                makePost(free, writer, "오늘 읽은 책 공유해요", "저는 해리포터 다시 읽는 중입니다.", false),
                makePost(free, writer, "전자책 단말기 추천 부탁", "크레마 vs 킨들 어떤게 좋을까요?", false),
                makePost(free, writer, "올해 목표 독서량은?", "저는 20권 목표입니다. 다들 몇 권 하시나요?", false),
                makePost(free, writer, "중고서점 꿀팁", "알라딘 중고 매장에서 득템했어요.", false),
                makePost(free, writer, "주말 독서 모임 모집", "서울에서 소규모로 독서모임 하실 분?", false),
                makePost(free, writer, "요즘 읽기 좋은 책?", "추천 좀 부탁드려요!", false)
        ));

        // 책추천 5
        postRepository.saveAll(List.of(
                makePost(recommend, writer, "추리소설 입문 추천", "애거서 크리스티 강추!", false),
                makePost(recommend, writer, "인생 에세이", "‘멈추면 비로소 보이는 것들’", false),
                makePost(recommend, writer, "경제 추천", "넛지 읽어보세요.", false),
                makePost(recommend, writer, "SF 추천", "삼체 어떠셨어요?", false),
                makePost(recommend, writer, "철학 입문", "소크라테스 익스프레스 추천", false)
        ));

        // 한줄글귀 8
        postRepository.saveAll(List.of(
                makePost(quotes, writer, "“길을 아는 것과 그 길을 걷는 것은 다르다.”", "", false),
                makePost(quotes, writer, "“과거에 머물지 말고 현재에 집중하라.”", "", false),
                makePost(quotes, writer, "“읽는다는 것은 다시 사는 일이다.”", "", false),
                makePost(quotes, writer, "“오늘의 나를 만든 것은 어제의 책이다.”", "", false),
                makePost(quotes, writer, "“지식은 공유할수록 커진다.”", "", false),
                makePost(quotes, writer, "“서두르지 말되 멈추지도 말라.”", "", false),
                makePost(quotes, writer, "“작은 습관이 커다란 변화를 만든다.”", "", false),
                makePost(quotes, writer, "“질문이 없는 곳에 배움도 없다.”", "", false)
        ));
    }

    private User upsert(String email, String nickname, Role role) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setEmail(email);
            u.setPassword(passwordEncoder.encode("1234"));
            u.setNickname(nickname);
            u.setHost(Host.LOCAL);
            u.setRole(role);
            return userRepository.save(u);
        });
    }

    private Post makePost(Board board, User user, String title, String content, boolean notice) {
        Post post = new Post();
        post.setBoard(board);
        post.setUser(user);
        post.setTitle(title);
        post.setContent(content == null || content.isBlank() ? "테스트용 내용입니다." : content);
        post.setViewCount((int)(Math.random()*50));
        post.setLikeCount((int)(Math.random()*10));
        post.setCommentCount(0);
        post.setCreatedAt(LocalDateTime.now().minusDays((long)(Math.random()*5)));
        post.setUpdatedAt(post.getCreatedAt());
        // 공지 플래그가 있으면:
        try { post.getClass().getMethod("setNotice", boolean.class).invoke(post, notice); } catch (Exception ignore) {}
        return post;
    }
}
