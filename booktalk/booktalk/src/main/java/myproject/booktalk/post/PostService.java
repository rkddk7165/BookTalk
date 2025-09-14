package myproject.booktalk.post;

import myproject.booktalk.post.dto.PostCreateRequest;
import myproject.booktalk.post.dto.PostDetailDto;
import myproject.booktalk.post.dto.PostRow;
import myproject.booktalk.post.dto.PostUpdateRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PostService {

    Long create(PostCreateRequest req);

    void update(PostUpdateRequest req);

    void delete(Long postId, Long requesterUserId);

    PostDetailDto getDetail(Long postId, boolean increaseView);

    // 카운터
    void increaseView(Long postId);
    void like(Long postId);          // 임시 단순 +1 (진짜 좋아요 토글은 별도 엔티티 필요)
    void unlike(Long postId);        // 임시 -1 클램프

    // 리스트(고정 게시판)
    Page<PostRow> listByBoard(Long boardId, String tab, String sort, int page, int size);
    List<PostRow> notices(Long boardId);
}
