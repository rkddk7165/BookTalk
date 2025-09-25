package myproject.booktalk.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import myproject.booktalk.user.controller.UserViewController;
import myproject.booktalk.user.dto.JoinRequest;
import myproject.booktalk.user.exception.UserException;
import myproject.booktalk.usersettings.UserSettings;
import myproject.booktalk.usersettings.UserSettingsRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserSettingsRepository settingsRepository;

    private static final Path UPLOAD_ROOT = Paths.get("uploads/profile"); // 프로젝트 루트 기준

    /** 회원가입 */
    public Long join(JoinRequest req) {
        // 이메일 중복 체크
        if (userRepository.existsByEmailAndHost(req.getEmail().trim().toLowerCase(), Host.LOCAL)) {
            throw new UserException("DUP_EMAIL", 400, "이미 가입된 이메일입니다.");
        }

        User user = new User();
        user.setEmail(req.getEmail().trim().toLowerCase());
        user.setNickname(req.getNickname().trim());
        user.setPassword(passwordEncoder.encode(req.getPassword().trim())); // ✅ 여기서 해시
        user.setHost(Host.LOCAL);
        user.setRole(Role.USER);

        return userRepository.save(user).getId();
    }


    /** 로그인  */
    public User login(String email, String rawPassword) {
        User u = userRepository.findByEmailAndHost(email.trim().toLowerCase(), Host.LOCAL)
                .orElseThrow(() -> new UserException("USER_NOT_FOUND", 400, "존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(rawPassword, u.getPassword())) { // ✅ matches 사용
            throw new UserException("INVALID_PASSWORD", 400, "비밀번호가 일치하지 않습니다.");
        }
        return u;
    }
    /**
     * 전체 회원 조회
     */
    public List<User> findAll(){
        return userRepository.findAll();
    }

    public void updateProfile(Long userId, String email, String nickname, String profileImageUrl) {
        User u = userRepository.findById(userId).orElseThrow();
        if (email != null && !email.isBlank()) u.setEmail(email.trim());
        if (nickname != null && !nickname.isBlank()) u.setNickname(nickname.trim());
        if (profileImageUrl != null && !profileImageUrl.isBlank()) u.setProfileImage(profileImageUrl);
        userRepository.save(u);
    }

    public String uploadProfileImage(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            if (!Files.exists(UPLOAD_ROOT)) Files.createDirectories(UPLOAD_ROOT);
            String ext = getExt(file.getOriginalFilename());
            String fname = "u" + userId + "_" + UUID.randomUUID() + (ext.isBlank() ? "" : "." + ext);
            Path dest = UPLOAD_ROOT.resolve(fname);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
            // 정적 리소스 매핑: /uploads/** → {project}/uploads/ 로 서빙되도록 WebMvc 설정 필요
            return "/uploads/profile/" + fname;
        } catch (IOException e) {
            throw new RuntimeException("프로필 이미지 업로드 실패", e);
        }
    }

    public void changePassword(Long userId, String currentPw, String newPw) {
        User u = userRepository.findById(userId).orElseThrow();
        String hash = u.getPassword();

        if (hash != null && !hash.isBlank()) {
            if (!passwordEncoder.matches(currentPw, hash)) {
                log.info("Current password = {}", currentPw);
                log.info("New password = {}", newPw);
                log.info("real password = {}", hash);
                throw new BadCredentialException("현재 비밀번호가 올바르지 않습니다.");
            }
        }
        u.setPassword(passwordEncoder.encode(newPw));
        userRepository.save(u);
    }

    public void saveSettings(Long userId, UserViewController.SettingsForm settings) {
        User u = userRepository.findById(userId).orElseThrow();
        UserSettings s = settingsRepository.findByUserId(userId).orElseGet(() -> new UserSettings(u));
        s.setEmailNotify(settings.isEmailNotify());
        s.setSocialNotify(settings.isSocialNotify());
        s.setProfilePublic(settings.isProfilePublic());
        settingsRepository.save(s);
    }

    @Transactional(readOnly = true)
    public UserViewController.SettingsForm loadSettings(Long userId) {
        return settingsRepository.findByUserId(userId)
                .map(s -> {
                    UserViewController.SettingsForm f = new UserViewController.SettingsForm();
                    f.setEmailNotify(Boolean.TRUE.equals(s.getEmailNotify()));
                    f.setSocialNotify(Boolean.TRUE.equals(s.getSocialNotify()));
                    f.setProfilePublic(Boolean.TRUE.equals(s.getProfilePublic()));
                    return f;
                })
                .orElseGet(UserViewController.SettingsForm::new);
    }

    public void deactivate(Long userId) {
        User u = userRepository.findById(userId).orElseThrow();
        u.setActive(false);
        userRepository.save(u);
    }

    public void deleteAccount(Long userId) {
        // 연관 데이터 정리는 프로젝트 정책에 맞게 (소프트삭제/소유권 이전/익명화 등)
        userRepository.deleteById(userId);
    }

    @Transactional(readOnly = true)
    public User findById(Long userId) {
        return userRepository.findById(userId).orElseThrow();
    }

    private String getExt(String name){
        if (name == null) return "";
        int i = name.lastIndexOf('.');
        return (i<0) ? "" : name.substring(i+1).toLowerCase();
    }




}
