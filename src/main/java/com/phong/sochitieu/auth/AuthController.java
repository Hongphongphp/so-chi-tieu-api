package com.phong.sochitieu.auth;

import com.phong.sochitieu.auth.AuthDtos.AuthResponse;
import com.phong.sochitieu.auth.AuthDtos.GoogleLoginRequest;
import com.phong.sochitieu.auth.AuthDtos.LoginRequest;
import com.phong.sochitieu.auth.AuthDtos.RegisterRequest;
import com.phong.sochitieu.common.ApiException;
import com.phong.sochitieu.user.User;
import com.phong.sochitieu.user.UserDtos.UserResponse;
import com.phong.sochitieu.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleTokenVerifier google;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                          GoogleTokenVerifier google) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.google = google;
    }

    /** Cấu hình công khai cho trang web (Client ID của Google không phải bí mật). */
    @GetMapping("/config")
    public Map<String, Object> config() {
        return Map.of("googleClientId", google.clientId());
    }

    /**
     * Đăng nhập bằng Google. Nếu email đã có tài khoản thì liên kết vào tài khoản đó,
     * nếu chưa thì tạo tài khoản mới (mật khẩu ngẫu nhiên, chỉ đăng nhập qua Google).
     */
    @PostMapping("/google")
    @Transactional
    public AuthResponse loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        if (!google.isEnabled()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Đăng nhập Google chưa được bật trên server");
        }
        GoogleTokenVerifier.GoogleUser g = google.verify(request.credential());
        String email = normalize(g.email());
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            String name = (g.name() == null || g.name().isBlank()) ? email.substring(0, email.indexOf('@')) : g.name().trim();
            if (name.length() > 60) name = name.substring(0, 60);
            return userRepository.save(new User(email, passwordEncoder.encode(UUID.randomUUID().toString()), name));
        });
        return new AuthResponse(jwtService.generateToken(user.getId()), UserResponse.from(user));
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        String email = normalize(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email này đã được đăng ký");
        }
        User user = userRepository.save(new User(
                email,
                passwordEncoder.encode(request.password()),
                request.displayName().trim()));
        return new AuthResponse(jwtService.generateToken(user.getId()), UserResponse.from(user));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(normalize(request.email()))
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng"));
        return new AuthResponse(jwtService.generateToken(user.getId()), UserResponse.from(user));
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
