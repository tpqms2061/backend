package com.ssh.backend.security;

import com.ssh.backend.entity.User;
import com.ssh.backend.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();


        //구글

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String avatarUrl = oAuth2User.getAttribute("picture");

        //깃허브에는 email 항목이 없을수도 있고 login 항목은 무조건 존재함  그래서 login@github.local형식으로 나타냄
        if (email == null && oAuth2User.getAttribute("login") != null) {
            email = oAuth2User.getAttribute("login") + "@github.local";
            avatarUrl = oAuth2User.getAttribute("avatar_url");
        }

        final String finalEmail = email;
        final String finalName = name != null ? name : "User";
        final String finalAvatarUrl = avatarUrl;

        User user = userRepository.findByEmail(finalEmail)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(finalEmail);
                    newUser.setUsername(generateUsername(finalName));
                    newUser.setProfileImageUrl(finalAvatarUrl);
                    newUser.setPassword("");
                    newUser.setEnabled(true);
                    newUser.setCreatedAt(LocalDateTime.now());
                    newUser.setUpdatedAt(LocalDateTime.now());
                    return userRepository.save(newUser);
                });
        if (finalAvatarUrl != null && !finalAvatarUrl.equals(user.getProfileImageUrl())) {
            user.setProfileImageUrl(finalAvatarUrl);
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        //백엔드가 프론트엔드로 토큰을 전달하기 위한 리다이렉트 URL을 조립하는 장치
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/callback") //리다이렉트할 페이지 경로 지정
                .queryParam("token", accessToken)
                .queryParam("refreshToken", refreshToken)  //accessToken과  refreshToken을 리다이렉트 경로에 포함
                .build().toUriString();  //문자열형태로 완성

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
        //사용자를 해당 URL로 리다렉트 시킴

    }
    //username 고유성 보장하기위한 장치
    private String generateUsername(String email) {
        String baseUsername = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
        //영문자와 숫자를 제외하고 삭제 =>  abd.def => abddef로 표현
        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
//counter를 붙임으로써 고유성 보장
        return username;
    }

}
