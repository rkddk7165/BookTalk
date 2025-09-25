package myproject.booktalk.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotBlank @Size(min = 8, max = 24)
    private String password;

    @NotBlank
    private String confirmPassword;

    private String profileImageUrl;

    private Boolean marketingOptIn = false;

    @NotNull private Boolean agreeTerms;
    @NotNull private Boolean agreePrivacy;
    @NotNull private Boolean over14;
}

