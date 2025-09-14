package myproject.booktalk.post;

import lombok.RequiredArgsConstructor;
import myproject.booktalk.post.dto.PostRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;     // ✅
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryServiceImpl implements PostQueryService {

    private final PostRepository postRepository;

    @Override
    public Page<PostRow> findFixedBoardPosts(Long boardId, String tab, String sort, int page, int size) {
        String _tab  = (tab == null || tab.isBlank()) ? "all"     : tab;
        String _sort = (sort == null || sort.isBlank()) ? "latest" : sort;

        Pageable pageable = PageRequest.of(page, size);  // ✅

        return switch (_tab) {
            case "best" -> switch (_sort) {
                case "popular" -> postRepository.findBestPopular(boardId, pageable);
                default        -> postRepository.findBestLatest(boardId, pageable);
            };
            case "notice" -> org.springframework.data.domain.Page.empty(pageable);
            default /* all */ -> switch (_sort) {
                case "popular" -> postRepository.findPopular(boardId, pageable);
                default        -> postRepository.findLatest(boardId, pageable);
            };
        };
    }

    @Override
    public List<PostRow> findFixedNotices(Long boardId) {
        return postRepository.findNotices(boardId);
    }


}
