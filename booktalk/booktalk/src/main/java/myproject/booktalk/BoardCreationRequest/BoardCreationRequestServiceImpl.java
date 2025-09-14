package myproject.booktalk.BoardCreationRequest;

import lombok.RequiredArgsConstructor;
import myproject.booktalk.board.service.BoardService;
import myproject.booktalk.book.Book;
import myproject.booktalk.book.BookRepository;
import myproject.booktalk.user.User;
import myproject.booktalk.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static myproject.booktalk.BoardCreationRequest.Status.*;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BoardCreationRequestServiceImpl implements BoardCreationRequestService {

    private final BoardCreationRequestRepository requestRepo;
    private final BookRepository bookRepo;
    private final UserRepository userRepo;
    private final BoardService boardService; // 책 1:1 게시판 생성에 사용

    /* ========== 생성/조회/취소(사용자) ========== */

    @Override
    @Transactional
    public Long createRequest(Long bookId, Long requesterUserId, String reason) {
        Book book = bookRepo.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 책입니다. id=" + bookId));
        User requester = userRepo.findById(requesterUserId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. id=" + requesterUserId));

        // 이미 책 게시판이 있지는 않은지 서비스단에서 추가로 검사하고 싶다면:
        // if (boardService.existsBookDiscussionByBookId(bookId)) throw new IllegalStateException("이미 게시판 존재");

        // 동일 책에 PENDING 요청이 이미 있는지 체크(중복 방지)
        if (requestRepo.existsByBook_IdAndStatus(bookId, PENDING)) {
            throw new DuplicateRequestException("이미 대기중인 요청이 있습니다.");
        }

        BoardCreationRequest r = new BoardCreationRequest();
        r.setBook(book);
        r.setUser(requester);
        r.setStatus(PENDING);
        r.setRejectReason(null);
        r.setRequestTime(LocalDateTime.now());
        r.setAcceptedTime(null);
        r.setAdmin(null);
        r.setRejectReason(reason);

        return requestRepo.save(r).getId();
    }

    @Override
    public BoardCreationRequest getMyRequest(Long requestId, Long requesterUserId) {
        BoardCreationRequest r = requestRepo.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException("요청을 찾을 수 없습니다."));
        if (!r.getUser().getId().equals(requesterUserId)) {
            throw new AccessDeniedException("본인 요청만 조회할 수 있습니다.");
        }
        return r;
    }

    @Override
    public Page<BoardCreationRequest> getMyRequests(Long requesterUserId, Pageable pageable) {
        return requestRepo.findByUser_IdOrderByIdDesc(requesterUserId, pageable);
    }

    @Override
    @Transactional
    public void cancel(Long requestId, Long requesterUserId) {
        BoardCreationRequest r = requestRepo.findForUpdate(requestId)
                .orElseThrow(() -> new RequestNotFoundException("요청을 찾을 수 없습니다."));

        if (!r.getUser().getId().equals(requesterUserId)) {
            throw new AccessDeniedException("본인 요청만 취소할 수 있습니다.");
        }
        if (r.getStatus() != PENDING) {
            throw new InvalidRequestStateException("대기중(PENDING) 상태에서만 취소할 수 있습니다.");
        }
        r.setStatus(CANCELED);
    }

    /* ========== 관리자 ========== */

    @Override
    public Page<BoardCreationRequest> getPendingRequests(Pageable pageable) {
        return requestRepo.findByStatus(PENDING, pageable);
    }

    @Override
    @Transactional
    public Long approve(Long requestId, Long adminUserId) {
        BoardCreationRequest r = requestRepo.findForUpdate(requestId)
                .orElseThrow(() -> new RequestNotFoundException("요청을 찾을 수 없습니다."));
        if (r.getStatus() != PENDING) {
            throw new InvalidRequestStateException("이미 처리된 요청입니다. 상태=" + r.getStatus());
        }

        User admin = userRepo.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다. id=" + adminUserId));

        Long bookId = r.getBook().getId();

        // 최종 중복 방지: 승인 직전에 보드 존재 재확인
        if (boardService.existsBookDiscussionByBookId(bookId)) {
            r.setStatus(REJECTED);
            r.setRejectReason("이미 동일 책 게시판이 존재합니다.");
            r.setAdmin(admin);
            r.setAcceptedTime(LocalDateTime.now());
            return null; // 혹은 이미 존재하는 보드 id를 찾아 반환하도록 정책화 가능
        }

        // 보드 생성(책 1:1 게시판)
        Long boardId = boardService.createBookDiscussion(bookId, adminUserId, /*title*/ null, /*desc*/ null);

        // 요청 상태 전이
        r.setStatus(ACCEPTED);
        r.setAdmin(admin);
        r.setAcceptedTime(LocalDateTime.now());
        // r.setApprovedBoardId(boardId); // 엔티티에 필드가 있다면 기록

        return boardId;
    }

    @Override
    @Transactional
    public void reject(Long requestId, Long adminUserId, String reason) {
        BoardCreationRequest r = requestRepo.findForUpdate(requestId)
                .orElseThrow(() -> new RequestNotFoundException("요청을 찾을 수 없습니다."));
        if (r.getStatus() != PENDING) {
            throw new InvalidRequestStateException("이미 처리된 요청입니다. 상태=" + r.getStatus());
        }
        User admin = userRepo.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다. id=" + adminUserId));

        r.setStatus(REJECTED);
        r.setRejectReason(reason);
        r.setAdmin(admin);
        r.setAcceptedTime(LocalDateTime.now());
    }

    /* ========== 예외 ========== */
    public static class RequestNotFoundException extends RuntimeException {
        public RequestNotFoundException(String msg) { super(msg); }
    }
    public static class DuplicateRequestException extends RuntimeException {
        public DuplicateRequestException(String msg) { super(msg); }
    }
    public static class InvalidRequestStateException extends RuntimeException {
        public InvalidRequestStateException(String msg) { super(msg); }
    }
    public static class AccessDeniedException extends RuntimeException {
        public AccessDeniedException(String msg) { super(msg); }
    }
}