package myproject.booktalk.init;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import myproject.booktalk.user.Host;
import myproject.booktalk.user.Role;
import myproject.booktalk.user.User;
import myproject.booktalk.user.UserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitData {

    private final UserRepository userRepository;

    @PostConstruct
    public void init() {
        User user = new User();
        user.setEmail("rkddk7165@naver.com");
        user.setPassword("1234");
        user.setNickname("민락주점");
        user.setHost(Host.LOCAL);

        User user2 = new User();
        user2.setEmail("admin@naver.com");
        user2.setPassword("1234");
        user2.setNickname("admin");
        user2.setHost(Host.LOCAL);
        user2.setRole(Role.ADMIN);

        userRepository.save(user);
        userRepository.save(user2);

    }
}
