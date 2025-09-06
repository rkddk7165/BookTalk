package myproject.booktalk.init;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import myproject.booktalk.user.Host;
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

        userRepository.save(user);

    }
}
