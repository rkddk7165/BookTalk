package myproject.booktalk.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class JoinRequest {
    @Email @NotBlank
    private String email;

    @NotBlank
    private String nickname;

    @NotBlank
    private String password;

    private String profileImage;
}

