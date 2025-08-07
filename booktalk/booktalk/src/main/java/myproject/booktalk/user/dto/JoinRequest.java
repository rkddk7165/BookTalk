package myproject.booktalk.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import myproject.booktalk.user.Host;
import myproject.booktalk.user.User;

@Data
@AllArgsConstructor
public class JoinRequest {

    @Email
    private String email;

    @NotBlank
    private String nickname;

    @NotBlank
    private String password;

    private String profileImage;


    /**
     *     Entity로 변환 편의 메소드
     */
    public User toEntity() {
        User user = new User();
        user.setEmail(this.email);
        user.setNickname(this.nickname);
        user.setPassword(this.password);
        user.setProfileImage(this.profileImage);
        return user;
    }
}
