package myproject.booktalk.BoardCreationRequest;

import jakarta.persistence.LockModeType;
import myproject.booktalk.BoardCreationRequest.BoardCreationRequest;
import myproject.booktalk.BoardCreationRequest.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardCreationRequestRepository extends JpaRepository<BoardCreationRequest, Long> {

    // 내 요청 목록
    Page<BoardCreationRequest> findByUser_IdOrderByIdDesc(Long requesterId, Pageable pageable);

    // 특정 책(Book) 기준 중복 요청 방지용
    boolean existsByBook_IdAndStatus(Long bookId, Status status);

    // 대기중 요청 페이징
    Page<BoardCreationRequest> findByStatus(Status status, Pageable pageable);

    // 승인 처리 시 동시성 제어(행 잠금)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from BoardCreationRequest r where r.id = :id")
    Optional<BoardCreationRequest> findForUpdate(@Param("id") Long id);

    @Query("""
        select r from BoardCreationRequest r
        join fetch r.book b
        join fetch r.user u
        where r.status = myproject.booktalk.BoardCreationRequest.Status.PENDING
        order by r.requestTime asc
    """)
    List<BoardCreationRequest> findPendings();

    @Query("""
       select r from BoardCreationRequest r
       join fetch r.user u
       join fetch r.book b
       order by r.requestTime desc
    """)
    Page<BoardCreationRequest> findAllWithJoins(Pageable pageable);

}
