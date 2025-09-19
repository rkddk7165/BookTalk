package myproject.booktalk.postReaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {

    Optional<PostReaction> findByPostIdAndUserId(Long postId, Long userId);

    // 없으면 1행 생성 (MySQL)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        INSERT IGNORE INTO post_reaction(post_id, user_id, liked, disliked, created_at)
        VALUES (:postId, :userId, false, false, NOW())
        """, nativeQuery = true)
    int ensureRow(Long postId, Long userId);

    // liked=false 인 경우에만 true로 변경 (1 반환 시에만 성공으로 간주)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update PostReaction r
           set r.liked = true
         where r.post.id = :postId
           and r.user.id = :userId
           and r.liked = false
        """)
    int markLikedIfNotYet(Long postId, Long userId);

    // disliked=false 인 경우에만 true로 변경
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update PostReaction r
           set r.disliked = true
         where r.post.id = :postId
           and r.user.id = :userId
           and r.disliked = false
        """)
    int markDislikedIfNotYet(Long postId, Long userId);
}
