/*
package myproject.booktalk.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import myproject.booktalk.kakao.KakaoLoginService;
import myproject.booktalk.user.User;
import myproject.booktalk.user.UserService;
import myproject.booktalk.user.dto.*;
import myproject.booktalk.user.session.SessionConst;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;
    private final KakaoLoginService kakaoLoginService;

    @PostMapping("/users")
    public JoinResponse join(@RequestBody JoinRequest request){
        User user = request.toEntity();
        Long id = userService.join(user);
        User findUser = userService.findById(id);

        return new JoinResponse(findUser.getId(), findUser.getNickname(), findUser.getProfileImage());
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request, HttpServletRequest httpServletRequest){
        User loginUser = userService.login(request.getEmail(), request.getPassword());

        // 세션 생성 및 저장
        HttpSession session = httpServletRequest.getSession();
        session.setAttribute(SessionConst.LOGIN_USER, loginUser);

        return new LoginResponse(loginUser.getId(), loginUser.getNickname());

    }

    @GetMapping("/myPage")
    public ResponseEntity<String> myPage(HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(SessionConst.LOGIN_USER);

        if (loginUser == null) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        String nickname = loginUser.getNickname();
        return ResponseEntity.ok("안녕하세요, " + nickname + "님!");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/oauth/kakao")
    public ResponseEntity<UserDto> kakaoLogin(
            @RequestBody Map<String, String> body,
            HttpServletRequest request
    ) {
        String code = body.get("code");

        User kakaoUser = kakaoLoginService.kakaoLogin(code);

        // 세션 저장
        HttpSession session = request.getSession();
        session.setAttribute(SessionConst.LOGIN_USER, kakaoUser);

        return ResponseEntity.ok(UserDto.from(kakaoUser));
    }
}
*/
