package myproject.booktalk.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

}
