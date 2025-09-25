package myproject.booktalk.comment;

import myproject.booktalk.post.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("""
        select c from Comment c
        join fetch c.user u
        where c.post.id = :postId and c.parent is null
        order by c.createdAt asc
""")
    List<Comment>
    findRoots(Long postId);

    @Query("""
        select c from Comment c
        join fetch c.user u
        where c.parent.id = :parentId
        order by c.createdAt asc
""")
    List<Comment> findChildren(Long parentId);

    @EntityGraph(attributePaths = {"user", "parent", "post"})
    List<Comment> findByPostId(Long postId);

    long countByUser_Id(Long userId);

    List<Comment> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
