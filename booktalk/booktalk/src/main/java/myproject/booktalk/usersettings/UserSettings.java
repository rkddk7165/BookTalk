package myproject.booktalk.usersettings;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import myproject.booktalk.user.User;

@Entity
@Getter @Setter
@NoArgsConstructor
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private Boolean emailNotify = false;
    private Boolean socialNotify = false;
    private Boolean profilePublic = true;

    public UserSettings(User user) {
        this.user = user;
    }
}
