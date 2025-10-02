package myproject.booktalk.post;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import myproject.booktalk.board.Board;
import myproject.booktalk.board.repository.BoardRepository;
import myproject.booktalk.post.dto.*;
import myproject.booktalk.postReaction.PostReactionRepository;
import myproject.booktalk.user.Role;
import myproject.booktalk.user.User;
import myproject.booktalk.user.UserRepository;
import myproject.booktalk.user.dto.TopWriterRow;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final PostReactionRepository reactionRepository;

    /* ===================== 생성 ===================== */
    @Override
    @Transactional
    public Long create(PostCreateRequest req) {
        Board board = boardRepository.findById(req.boardId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판입니다. id=" + req.boardId()));
        User user = userRepository.findById(req.userId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. id=" + req.userId()));

        // 권한: 공지/개념글은 관리자만 허용 (정책)
        boolean notice = req.isNotice();
        boolean best   = req.isBest();
        if (!isAdmin(user)) {
            if (notice) notice = false;
            if (best)   best   = false;
        }

        Post p = new Post();
        p.setBoard(board);
        p.setUser(user);
        p.setTitle(req.title());
        p.setContent(req.content());
        p.setViewCount(0);
        p.setLikeCount(0);
        p.setCommentCount(0);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        p.setNotice(notice);
        p.setBest(best);

        postRepository.save(p);
        return p.getId();
    }

    /* ===================== 수정 ===================== */
    @Override
    @Transactional
    public void update(PostUpdateRequest req) {
        Post p = postRepository.findForUpdate(req.postId())
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다. id=" + req.postId()));

        User editor = userRepository.findById(req.editorUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. id=" + req.editorUserId()));

        // 권한: 작성자 또는 관리자만 수정
        if (!isAdmin(editor) && !p.getUser().getId().equals(editor.getId())) {
            throw new SecurityException("수정 권한이 없습니다.");
        }

        if (req.title() != null)   p.setTitle(req.title());
        if (req.content() != null) p.setContent(req.content());
        if (req.isNotice() != null && isAdmin(editor)) p.setNotice(req.isNotice());
        if (req.isBest()   != null && isAdmin(editor)) p.setBest(req.isBest());

        p.setUpdatedAt(LocalDateTime.now());
    }

    /* ===================== 삭제 ===================== */
    @Override
    @Transactional
    public void delete(Long postId, Long requesterUserId) {
        Post p = postRepository.findForUpdate(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다. id=" + postId));

        User actor = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. id=" + requesterUserId));

        if (!isAdmin(actor) && !p.getUser().getId().equals(actor.getId())) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }
        postRepository.delete(p);
    }

    /* ===================== 상세 조회 ===================== */
    @Override
    @Transactional
    public PostDetailDto getDetail(Long postId, boolean increaseView) {
        if (increaseView) {
            // 동시성에 안전한 +1
            postRepository.incrementViewCount(postId);
        }
        Post p = postRepository.findDetail(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다. id=" + postId));

        return new PostDetailDto(
                p.getId(),
                p.getBoard().getId(),
                p.getUser().getId(),
                p.getUser().getNickname(),
                p.getTitle(),
                p.getContent(),
                nz(p.getViewCount()),
                nz(p.getLikeCount()),
                nz(p.getDislikeCount()),
                nz(p.getCommentCount()),
                p.isNotice(),
                p.isBest(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    /* ===================== 카운터 ===================== */
    @Override @Transactional
    public void increaseView(Long postId) {
        postRepository.incrementViewCount(postId);
    }


    @Override
    @Transactional
    public void like(Long postId, Long userId) {
        if (!postRepository.existsById(postId)) {
            throw new EmptyResultDataAccessException("게시글 없음", 1);
        }

        reactionRepository.ensureRow(postId, userId);

        int updated = reactionRepository.markLikedIfNotYet(postId, userId);
        if (updated == 0) {
            throw new IllegalStateException("이미 좋아요를 누르셨습니다.");
        }

        postRepository.incrementLikeCount(postId);
    }

    @Override
    @Transactional
    public void dislike(Long postId, Long userId) {
        if (!postRepository.existsById(postId)) {
            throw new EmptyResultDataAccessException("게시글 없음", 1);
        }

        reactionRepository.ensureRow(postId, userId);

        int updated = reactionRepository.markDislikedIfNotYet(postId, userId);
        if (updated == 0) {
            throw new IllegalStateException("이미 싫어요를 누르셨습니다.");
        }

        postRepository.incrementDislikeCount(postId);
    }

    @Override
    public long countByUserId(Long userId) {
        return postRepository.countByUser_Id(userId);
    }

    @Override
    public List<Post> findRecentByUserId(Long userId, Pageable pageable) {
        return postRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable);
    }

    /* ===================== 리스트(고정 게시판) ===================== */
    @Override
    public Page<PostRow> listByBoard(Long boardId, String tab, String sort, int page, int size) {
        String _tab  = (tab == null || tab.isBlank()) ? "all" : tab;
        String _sort = (sort == null || sort.isBlank()) ? "latest" : sort;
        var pageable = PageRequest.of(page, size);

        log.info("tab = {}", _tab);
        log.info("sort = {}", _sort);

        Page<PostRow> raw = switch (_tab) {
            case "best" -> switch (_sort) {
                case "popular" -> postRepository.findBestPopular(boardId, pageable);
                default        -> postRepository.findBestLatest(boardId, pageable);
            };
            case "notice" -> Page.empty(pageable);
            default -> switch (_sort) {
                case "popular" -> postRepository.findPopular(boardId, pageable);
                default        -> postRepository.findLatest(boardId, pageable);
            };
        };

        // 가상 번호: total - (page*size) - idx
        long start = raw.getTotalElements() - (long) page * size;
        List<PostRow> mapped = new java.util.ArrayList<>(raw.getContent().size());
        for (int i = 0; i < raw.getContent().size(); i++) {
            PostRow p = raw.getContent().get(i);
            long num = Math.max(start - i, 1);
            mapped.add(new PostRow(
                    p.id(), p.title(), p.authorNickname(), p.createdAt(),
                    p.viewCount(), p.likeCount(), p.dislikeCount(),
                    p.commentCount(), p.isNotice(), p.isBest(), num
            ));
        }
        return new org.springframework.data.domain.PageImpl<>(mapped, pageable, raw.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopWriterRow> findTopWriters(int limit) {
        return postRepository.findTopWriters(PageRequest.of(0, limit));
    }



    @Override
    public List<PostRow> notices(Long boardId) {
        return postRepository.findNotices(boardId);
    }

    /* ===================== 유틸 ===================== */
    private boolean isAdmin(User u) {
        return u.getRole() != null && u.getRole() == Role.ADMIN;
    }
    private int nz(Integer v) { return v == null ? 0 : v; }
}