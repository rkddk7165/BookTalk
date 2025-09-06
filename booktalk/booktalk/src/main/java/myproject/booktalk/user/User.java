package myproject.booktalk.user;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@Entity
@Getter @Setter
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true) // ← email 필드가 있어도 null 가능하게
    private String email;
    private String nickname;
    private String password;

    @Lob
    @Column(name = "profile_image")
    private String profileImage;

    @Enumerated(EnumType.STRING)
    private Host host;

    @Column(name = "sns_id")
    private String snsId;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    public User(){};

    public User(String email, String nickname, String profileImage, Host host, String snsId) {
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.snsId = null;
        this.password = ""; // 소셜 로그인은 패스워드 비움
    }

}
