// KakaoLoginService.java - 이메일 없이 로그인 처리

package myproject.booktalk.kakao;

import lombok.RequiredArgsConstructor;
import myproject.booktalk.user.User;
import myproject.booktalk.user.UserRepository;
import myproject.booktalk.user.Host;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoLoginService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.token-uri}")
    private String tokenUri;

    @Value("${kakao.user-info-uri}")
    private String userInfoUri;

    public User kakaoLogin(String code) {
        String accessToken = getAccessToken(code);
        Map<String, Object> userInfo = getUserInfo(accessToken);

        Long kakaoId = ((Number) userInfo.get("id")).longValue();

        Map<String, Object> profile = null;
        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
        if (kakaoAccount != null) {
            profile = (Map<String, Object>) kakaoAccount.get("profile");
        }

        final String nickname = (profile != null) ? (String) profile.getOrDefault("nickname", "카카오사용자") : "카카오사용자";
        final String profileImage = (profile != null) ? (String) profile.get("profile_image_url") : null;

        return userRepository.findBySnsIdAndHost(kakaoId.toString(), Host.KAKAO)
                .orElseGet(() -> {
                    User newUser = new User(null, nickname, profileImage, Host.KAKAO, kakaoId.toString());
                    return userRepository.save(newUser);
                });
    }

    private String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUri, request, Map.class);
        return (String) response.getBody().get("access_token");
    }

    private Map<String, Object> getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUri,
                HttpMethod.GET,
                request,
                Map.class
        );

        return response.getBody();
    }



    public String buildAuthorizeUrl() {
        String base = "https://kauth.kakao.com/oauth/authorize";
        String params = String.format(
                "?response_type=code&client_id=%s&redirect_uri=%s",
                clientId, URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
        );
        return base + params;
    }
}
