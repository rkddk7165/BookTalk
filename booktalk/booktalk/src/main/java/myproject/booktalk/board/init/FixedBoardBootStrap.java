package myproject.booktalk.board.init;

import lombok.RequiredArgsConstructor;
import myproject.booktalk.board.service.BoardService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * 서버 기동 시, 고정 게시판(자유/책추천/한줄글귀)을 보장한다.
 * 이미 있으면 건너뛰고, 없으면 생성한다 (아이템포턴트).
 */
@Configuration
@RequiredArgsConstructor
class FixedBoardBootstrap {

    private final BoardService boardService;

    @Bean
    @Order(1)  // ✅ 가장 먼저 실행되도록 지정
    ApplicationRunner initFixedBoards() {
        return args -> {
            boardService.ensureFixedBoard("자유게시판", "자유롭게 대화하는 공간", null);
            boardService.ensureFixedBoard("책추천게시판", "좋은 책을 추천해주세요", null);
            boardService.ensureFixedBoard("한줄글귀게시판", "한 줄 글귀를 남겨보세요", null);
        };
    }
}
