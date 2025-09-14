package myproject.booktalk.post;

import myproject.booktalk.post.dto.PostRow;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PostQueryService {

    public Page<PostRow> findFixedBoardPosts(Long boardId, String tab, String sort, int page, int size);

    public List<PostRow> findFixedNotices(Long boardId);
}
