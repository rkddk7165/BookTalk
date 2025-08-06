package myproject.booktalk.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JoinResponse {

    private Long id;
    private String nickname;
    private String profileImage;

}
