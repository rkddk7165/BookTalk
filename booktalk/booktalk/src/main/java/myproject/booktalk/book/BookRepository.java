package myproject.booktalk.book;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface BookRepository extends CrudRepository<Book, Long> {
    Optional<Book> findByIsbn(String isbn13);

    boolean existsByIsbn(String isbn13);

    Book saveAndFlush(Book b);
}
