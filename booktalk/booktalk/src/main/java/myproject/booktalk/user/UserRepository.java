package myproject.booktalk.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndHost(String email, Host host);

    Optional<User> findBySnsIdAndHost(String string, Host host);
}
