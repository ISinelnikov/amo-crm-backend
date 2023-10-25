package oss.backend.controller;

import oss.backend.security.AuthenticationDetails;
import oss.backend.service.SecurityService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.requireNonNull;
import static oss.backend.util.OSSStringUtils.valueToNull;

@RestController
@RequestMapping("/api/security")
public class SecurityController {
    private final SecurityService securityService;

    public SecurityController(SecurityService securityService) {
        this.securityService = securityService;
    }

    @PostMapping(path = "/one-factor-login")
    public ResponseEntity<AuthenticationDetails> oneFactorAuthenticate(@RequestBody OneFactorCredentialsDto credentials) {
        return ResponseEntity.ok(securityService.oneFactorAuthenticate(credentials.username(), credentials.password()));
    }

    @PostMapping(path = "/two-factor-login")
    public ResponseEntity<AuthenticationDetails> oneFactorAuthenticate(@RequestBody TwoFactorCredentialsDto credentials) {
        return ResponseEntity.ok(securityService.twoFactorAuthenticate(credentials.requestId(), credentials.code()));
    }

    public record OneFactorCredentialsDto(String username, String password) {
        @JsonCreator
        public OneFactorCredentialsDto(@JsonProperty("username") String username, @JsonProperty("password") String password) {
            this.username = requireNonNull(valueToNull(username), "login can't be null");
            this.password = requireNonNull(valueToNull(password), "password can't be null");
        }
    }

    public record TwoFactorCredentialsDto(String requestId, int code) {
        @JsonCreator
        public TwoFactorCredentialsDto(@JsonProperty("requestId") String requestId, @JsonProperty("code") int code) {
            this.requestId = requireNonNull(valueToNull(requestId), "requestId can't be null");
            this.code = code;
        }
    }
}
