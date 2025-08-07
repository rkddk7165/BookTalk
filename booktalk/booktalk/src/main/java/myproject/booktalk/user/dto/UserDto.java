package myproject.booktalk.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import myproject.booktalk.user.User;

@Data
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String nickname;

    public static UserDto from(User user) {
        return new UserDto(user.getId(), user.getNickname());
    }
}