package myproject.booktalk.user;

import lombok.RequiredArgsConstructor;
import myproject.booktalk.user.exception.UserException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 회원가입
     */
    public Long join(User user) {
        User savedUser = userRepository.save(user);

        // 로컬 가입자에 대해 host와 snsId 강제 설정
        user.setHost(Host.LOCAL);
        user.setSnsId(null);

        return savedUser.getId();
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User login(String email, String password) {
        User findUser = userRepository.findByEmailAndHost(email, Host.LOCAL)
                .orElseThrow(() -> new UserException(
                        "USER_NOT_FOUND",
                        400,
                        "존재하지 않는 사용자입니다."
                ));

        if(!findUser.getPassword().equals(password)) {
            throw new UserException(
                    "INVALID_PASSWORD",
                    400,
                    "비밀번호가 일치하지 않습니다."
            );
        }
        return findUser;
    }


    /**
     * 전체 회원 조회
     */
    public List<User> findAll(){
        return userRepository.findAll();
    }
}
