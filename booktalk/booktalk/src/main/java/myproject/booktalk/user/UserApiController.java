package myproject.booktalk.user;

import lombok.RequiredArgsConstructor;
import myproject.booktalk.user.dto.JoinRequest;
import myproject.booktalk.user.dto.JoinResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;

    @PostMapping("/users")
    public JoinResponse join(@RequestBody JoinRequest request){
        User user = request.toEntity();
        Long id = userService.join(user);
        User findUser = userService.findById(id);

        return new JoinResponse(findUser.getId(), findUser.getNickname(), findUser.getProfileImage());
    }
}
