package myproject.booktalk.board.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import myproject.booktalk.board.dto.ActiveBoardItem;
import myproject.booktalk.board.dto.BoardSearch;
import myproject.booktalk.board.BoardType;
import myproject.booktalk.board.dto.BookDiscussionBoardItem;
import myproject.booktalk.post.QPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

import static myproject.booktalk.board.QBoard.board;
import static myproject.booktalk.book.QBook.book;


@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public Page<BookDiscussionBoardItem> searchBookDiscussion(BoardSearch cond, Pageable pageable) {

        BooleanBuilder builder = new BooleanBuilder()
                .and(board.boardType.eq(BoardType.BOOK_DISCUSSION))
                .and(titleContains(cond.getTitle()))
                .and(authorContains(cond.getAuthor()));

        List<BookDiscussionBoardItem> content = query
                .select(Projections.constructor(
                        BookDiscussionBoardItem.class,
                        board.id,
                        book.id,
                        book.title.coalesce(book.title),
                        book.author
                ))
                .from(board)
                .leftJoin(board.book, book)
                .where(builder)
                .orderBy(book.title.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = query
                .select(board.count())
                .from(board)
                .leftJoin(board.book, book)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);


    }

    @Override
    public List<BookDiscussionBoardItem> findRecentBookDiscussion(int limit) {
        return query
                .select(Projections.constructor(
                        BookDiscussionBoardItem.class,
                        board.id,
                        book.id,
                        board.title.coalesce(book.title),
                        book.author
                ))
                .from(board)
                .leftJoin(board.book, book)
                .where(board.boardType.eq(BoardType.BOOK_DISCUSSION))
                .orderBy(board.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<ActiveBoardItem> findHotBookDiscussion(int limit) {
        QPost p = QPost.post;

        return query
                .select(Projections.constructor(
                        ActiveBoardItem.class,
                        board.id,
                        board.title.coalesce(book.title),
                        book.author,
                        p.count()
                ))
                .from(p)
                .join(p.board, board)
                .leftJoin(board.book, book)
                .where(board.boardType.eq(BoardType.BOOK_DISCUSSION))
                .groupBy(board.id, board.title, book.title, book.author)
                .orderBy(p.count().desc(), board.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    private BooleanExpression titleContains(String title) {
        if (!StringUtils.hasText(title)) return null;
        // 게시판 제목(board.title) 기준 (=책 제목으로 사용한다고 했음)
        return board.title.containsIgnoreCase(title.trim());
    }

    private BooleanExpression authorContains(String authorName) {
        if (!StringUtils.hasText(authorName)) return null;
        // 책 저자(book.author)
        return book.author.containsIgnoreCase(authorName.trim());
    }
}
