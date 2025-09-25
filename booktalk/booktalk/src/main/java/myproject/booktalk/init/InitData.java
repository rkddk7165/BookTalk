package myproject.booktalk.init;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import myproject.booktalk.user.Host;
import myproject.booktalk.user.Role;
import myproject.booktalk.user.User;
import myproject.booktalk.user.UserRepository;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitData {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        User user = new User();
        user.setEmail("rkddk7165@naver.com");
        user.setPassword(passwordEncoder.encode("1234"));
        user.setNickname("민락주점");
        user.setHost(Host.LOCAL);

        User user2 = new User();
        user2.setEmail("admin@naver.com");
        user2.setPassword(passwordEncoder.encode("1234"));
        user2.setNickname("관리자");
        user2.setHost(Host.LOCAL);
        user2.setRole(Role.ADMIN);

        User user3 = new User();
        user3.setEmail("1111@naver.com");
        user3.setPassword(passwordEncoder.encode("1234"));
        user3.setNickname("카이사");
        user3.setHost(Host.LOCAL);
        user3.setRole(Role.USER);

        userRepository.save(user);
        userRepository.save(user2);
        userRepository.save(user3);

    }
}
