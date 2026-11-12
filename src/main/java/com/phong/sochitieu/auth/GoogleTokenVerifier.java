package com.phong.sochitieu.auth;

import com.phong.sochitieu.common.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.Set;

/**
 * Xác minh "ID token" mà nút Đăng nhập Google trả về cho trình duyệt.
 * Gọi endpoint tokeninfo của Google (Google tự kiểm tra chữ ký và hạn dùng),
 * sau đó tự kiểm tra token được cấp cho đúng app này (aud) và email đã xác minh.
 */
@Service
public class GoogleTokenVerifier {

    public record GoogleUser(String email, String name) {
    }

    private static final Set<String> ISSUERS = Set.of("accounts.google.com", "https://accounts.google.com");

    private final String clientId;
    private final RestClient http = RestClient.create();

    public GoogleTokenVerifier(@Value("${app.google.client-id:}") String clientId) {
        this.clientId = clientId == null ? "" : clientId.trim();
    }

    public boolean isEnabled() {
        return !clientId.isEmpty();
    }

    public String clientId() {
        return clientId;
    }

    public GoogleUser verify(String idToken) {
        Map<String, Object> info;
        try {
            info = http.get()
                    .uri("https://oauth2.googleapis.com/tokeninfo?id_token={token}", idToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
        } catch (RestClientException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Đăng nhập Google không hợp lệ, vui lòng thử lại");
        }
        if (info == null
                || !clientId.equals(String.valueOf(info.get("aud")))
                || !ISSUERS.contains(String.valueOf(info.get("iss")))
                || !"true".equals(String.valueOf(info.get("email_verified")))
                || info.get("email") == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Đăng nhập Google không hợp lệ, vui lòng thử lại");
        }
        Object name = info.get("name");
        return new GoogleUser(String.valueOf(info.get("email")), name == null ? null : String.valueOf(name));
    }
}
