-- Post: 게시판별 최신글 조회
CREATE INDEX idx_post_board_created
    ON post (board_id, created_at DESC);

-- Comment: 게시글별 댓글 조회
CREATE INDEX idx_comment_post_created
    ON comment (post_id, created_at ASC);

-- PostReaction: 중복 반응 방지
ALTER TABLE post_reaction
    ADD CONSTRAINT ux_postreaction_post_user UNIQUE (post_id, user_id);

-- Board: 책 기반 게시판 조회
CREATE INDEX idx_board_book
    ON board (book_id);
