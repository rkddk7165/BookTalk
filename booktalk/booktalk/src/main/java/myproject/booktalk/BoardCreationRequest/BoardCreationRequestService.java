package myproject.booktalk.BoardCreationRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardCreationRequestService {

    /** 생성 요청 등록(동일 책에 기존 PENDING 있으면 예외) */
    Long createRequest(Long bookId, Long requesterUserId, String reason);

    /** 내 요청 단건 조회(권한 체크 포함) */
    BoardCreationRequest getMyRequest(Long requestId, Long requesterUserId);

    /** 내 요청 목록 */
    Page<BoardCreationRequest> getMyRequests(Long requesterUserId, Pageable pageable);

    /** 내 요청 취소 (PENDING에서만 가능) */
    void cancel(Long requestId, Long requesterUserId);

    /** (관리자) 대기중 요청 목록 조회 */
    Page<BoardCreationRequest> getPendingRequests(Pageable pageable);

    /**
     * (관리자) 승인 → 책 1:1 게시판 생성까지 트랜잭션으로 처리
     * @return 생성된 Board ID
     */
    Long approve(Long requestId, Long adminUserId);

    /** (관리자) 반려 */
    void reject(Long requestId, Long adminUserId, String reason);
}