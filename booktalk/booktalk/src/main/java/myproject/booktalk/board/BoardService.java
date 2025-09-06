package myproject.booktalk.board;

import lombok.RequiredArgsConstructor;
import myproject.booktalk.BoardCreationRequest.BoardCreationRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    /**
     *  게시판 생성 요청 (일반 사용자)
     *
     */
    public Long createRequest(BoardCreationRequest req, Long userId) {
        return userId;
    }
}
