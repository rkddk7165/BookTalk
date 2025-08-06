package myproject.booktalk.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 회원가입
     */
    public Long join(User user) {
        User savedUser = userRepository.save(user);
        return savedUser.getId();
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }


    /**
     * 전체 회원 조회
     */
    public List<User> findAll(){
        return userRepository.findAll();
    }
}
