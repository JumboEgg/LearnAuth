package ssafy.d210.backend.entity;
//
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import ssafy.d210.backend.dto.request.user.SignupRequest;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @NotNull
    @Length(min = 6, max = 30)
    private String email;

    @NotNull
    @Length(min = 8, max = 100)
    private String password;

    @NotNull
    @Length(max = 10)
    private String nickname;

    @NotNull
    @Length(max = 64)
    private String wallet;

    @NotNull
    @Length(max = 16)
    private String name;

<<<<<<< HEAD
    @NotNull
    @Length(max=64)
    private String userKey;

    @OneToMany(mappedBy = "user")
    private List<UserLecture> userLectureList;

    @OneToMany(mappedBy = "user")
    private List<PaymentRatio> paymentRatioList;

=======
    private String refreshToken;
>>>>>>> origin/master

    public void createUser(SignupRequest userSignupRequest) {
        this.email = userSignupRequest.getEmail();
        this.password = userSignupRequest.getPassword();
        this.nickname = userSignupRequest.getNickname();
        this.wallet = userSignupRequest.getWallet();
        this.name = userSignupRequest.getName();
        this.refreshToken = "";
    }
}
