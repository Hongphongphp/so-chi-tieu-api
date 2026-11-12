package com.phong.sochitieu.auth;

import com.phong.sochitieu.auth.AuthDtos.AuthResponse;
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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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
