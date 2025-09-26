package myproject.booktalk.post;

import jakarta.persistence.LockModeType;
import myproject.booktalk.post.dto.PostRow;
import myproject.booktalk.user.dto.TopWriterRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;  // ✅
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
           select new myproject.booktalk.post.dto.PostRow(
               p.id, p.title, p.user.nickname, p.createdAt,
               p.viewCount, p.likeCount, p.dislikeCount,
               p.commentCount, p.isNotice, p.isBest, null
           )
           from Post p
           where p.board.id = :boardId and p.isNotice = true
           order by p.createdAt desc
           """)
    List<PostRow> findNotices(Long boardId);

    @Query("""
           select new myproject.booktalk.post.dto.PostRow(
               p.id, p.title, p.user.nickname, p.createdAt,
               p.viewCount, p.likeCount, p.dislikeCount,
               p.commentCount, p.isNotice, p.isBest, null
           )
           from Post p
           where p.board.id = :boardId and p.isNotice = false
           order by p.createdAt desc
           """)
    Page<PostRow> findLatest(Long boardId, Pageable pageable);

    @Query("""
           select new myproject.booktalk.post.dto.PostRow(
               p.id, p.title, p.user.nickname, p.createdAt,
               p.viewCount, p.likeCount, p.dislikeCount,
               p.commentCount, p.isNotice, p.isBest, null
           )
           from Post p
           where p.board.id = :boardId and p.isNotice = false
           order by p.likeCount desc, p.viewCount desc, p.createdAt desc
           """)
    Page<PostRow> findPopular(Long boardId, Pageable pageable);

    @Query("""
           select new myproject.booktalk.post.dto.PostRow(
               p.id, p.title, p.user.nickname, p.createdAt,
               p.viewCount, p.likeCount, p.dislikeCount,
               p.commentCount, p.isNotice, p.isBest, null
           )
           from Post p
           where p.board.id = :boardId and p.isBest = true and p.isNotice = false
           order by p.createdAt desc
           """)
    Page<PostRow> findBestLatest(Long boardId, Pageable pageable);

    @Query("""
           select new myproject.booktalk.post.dto.PostRow(
               p.id, p.title, p.user.nickname, p.createdAt,
               p.viewCount, p.likeCount, p.dislikeCount,
               p.commentCount, p.isNotice, p.isBest, null
           )
           from Post p
           where p.board.id = :boardId and p.isBest = true and p.isNotice = false
           order by p.likeCount desc, p.viewCount desc, p.createdAt desc
           """)
    Page<PostRow> findBestPopular(Long boardId, Pageable pageable);



    /* ===== 상세 조회(필요 연관만 페치) ===== */
    @Query("""
           select p from Post p
           join fetch p.board b
           join fetch p.user u
           where p.id = :postId
           """)
    Optional<Post> findDetail(Long postId);

    long countByUser_Id(Long userId);

    List<Post> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /* ===== 카운터/락 ===== */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Post p where p.id = :postId")
    Optional<Post> findForUpdate(Long postId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Post p set p.viewCount = coalesce(p.viewCount,0) + 1 where p.id = :postId")
    int incrementViewCount(Long postId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Post p set p.likeCount = coalesce(p.likeCount,0) + 1 where p.id = :postId")
    int incrementLikeCount(Long postId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Post p set p.dislikeCount = coalesce(p.dislikeCount, 0) + 1 where p.id = :postId ")
    int incrementDislikeCount(Long postId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Post p set p.commentCount = coalesce(p.commentCount,0) + :delta where p.id = :postId")
    int bumpCommentCount(Long postId, int delta);


    //TopWriters 집계
    @Query("""
           select new myproject.booktalk.user.dto.TopWriterRow(u.id, u.nickname, count(p))
           from Post p join p.user u
           group by u.id, u.nickname
           order by count(p) desc
           """)
    List<TopWriterRow> findTopWriters(Pageable pageable);





}
