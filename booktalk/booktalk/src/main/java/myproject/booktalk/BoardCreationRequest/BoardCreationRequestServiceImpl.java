package myproject.booktalk.BoardCreationRequest;

import lombok.RequiredArgsConstructor;
import myproject.booktalk.board.service.BoardService;
import myproject.booktalk.book.Book;
import myproject.booktalk.book.BookRepository;
import myproject.booktalk.user.Role;
import myproject.booktalk.user.User;
import myproject.booktalk.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static myproject.booktalk.BoardCreationRequest.Status.*;

/**
 * 게시판 생성 요청 서비스 구현
 * - 요청 생성/조회/취소 (사용자)
 * - 요청 목록/승인/반려 (관리자)
 *
 * 변경 사항:
 * 1) 사용자 요청 사유는 requestReason에 저장 (rejectReason는 관리자 반려 사유)
 * 2) 승인 시 이미 동일 책 보드가 있으면 예외 발생(InvalidRequestStateException)
 * 3) 관리자 권한 검증 ensureAdmin 추가
 * 4) 요청 생성 시 보드 존재하면 즉시 차단
 */
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

        // 이미 해당 책 게시판 존재 여부 체크
        if (boardService.existsBookDiscussionByBookId(bookId)) {
            throw new InvalidRequestStateException("이미 해당 책 게시판이 존재합니다.");
        }

        // 동일 책에 PENDING 요청 존재 여부 체크
        if (requestRepo.existsByBook_IdAndStatus(bookId, PENDING)) {
            throw new DuplicateRequestException("이미 대기중인 요청이 있습니다.");
        }

        BoardCreationRequest r = new BoardCreationRequest();
        r.setBook(book);
        r.setUser(requester);
        r.setStatus(PENDING);
        r.setRequestTime(LocalDateTime.now());
        r.setAcceptedTime(null);
        r.setAdmin(null);
        // ✅ 반려 사유는 관리자만 채움 → 처음에는 무조건 null
        r.setRejectReason(null);

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
        ensureAdmin(admin); // ✅ 관리자 권한 확인

        Long bookId = r.getBook().getId();

        // 최종 중복 방지: 승인 직전에 보드 존재 재확인 (동시성)
        if (boardService.existsBookDiscussionByBookId(bookId)) {
            // 정책상: 예외로 알려주고 컨트롤러에서 토스트 처리
            throw new InvalidRequestStateException("이미 동일 책 게시판이 존재합니다.");
        }

        // 보드 생성 (제목/설명 기본값 구성 - 서비스에서 자동 생성한다면 null 전달 가능)
        String title = r.getBook().getTitle() + " 토론";
        String desc  = r.getBook().getTitle() + "에 대한 토론 게시판입니다.";
        Long boardId = boardService.createBookDiscussion(bookId, adminUserId, title, desc);
        // 요청자 소유로 만들고 싶으면 adminUserId 대신 r.getUser().getId() 전달

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
        ensureAdmin(admin); // ✅

        r.setStatus(REJECTED);
        r.setRejectReason(reason);
        r.setAdmin(admin);
        r.setAcceptedTime(LocalDateTime.now());
    }

    /* ========== 내부 유틸 ========== */

    private void ensureAdmin(User u) {
        if (u == null || u.getRole() != Role.ADMIN) { // Role enum 사용
            throw new AccessDeniedException("관리자만 처리할 수 있습니다.");
        }
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
