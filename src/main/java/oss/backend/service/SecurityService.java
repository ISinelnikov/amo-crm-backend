package oss.backend.service;

import oss.backend.domain.Profile;
import oss.backend.exception.UnauthorizedException;
import oss.backend.repository.core.SecurityRepository;
import oss.backend.security.AuthenticationDetails;
import oss.backend.security.AuthenticationRequest;
import oss.backend.security.TwoFactorAuthType;
import oss.backend.util.JwtUtils;
import oss.backend.util.SecurityUtils;
import oss.backend.util.SequenceUtils;

import javax.annotation.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;

@Service
public class SecurityService {
    private final AuthenticationManager authenticationManager;
    private final GoogleAuthenticator googleAuthenticator;
    private final JwtUtils jwtUtils;
    private final SecurityRepository securityRepository;

    public SecurityService(AuthenticationManager authenticationManager, GoogleAuthenticator googleAuthenticator,
            JwtUtils jwtUtils, SecurityRepository securityRepository) {
        this.authenticationManager = authenticationManager;
        this.googleAuthenticator = googleAuthenticator;
        this.jwtUtils = jwtUtils;
        this.securityRepository = securityRepository;
    }

    public AuthenticationDetails oneFactorAuthenticate(String username, String password) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        Profile profile = SecurityUtils.getCurrentUser(authenticate).getProfile();
        TwoFactorAuthType authType = profile.authType();
        String requestId = SequenceUtils.getCharactersToken(username.length());
        if (authType == TwoFactorAuthType.NONE) {
            securityRepository.saveOneFactorAuthenticationRequest(requestId, profile.id());
            return AuthenticationDetails.token(generateToken(username, authenticate));
        }
        Integer twoFactorCode = sendTwoFactorCode(profile.id(), authType);
        securityRepository.saveTwoFactorAuthenticationRequest(requestId, profile.id(), authType, twoFactorCode);
        return AuthenticationDetails.details(authType, requestId);
    }

    @Nullable
    private static Integer sendTwoFactorCode(long profileId, TwoFactorAuthType authType) {
        return null;
    }

    public AuthenticationDetails twoFactorAuthenticate(String requestId, int code) {
        AuthenticationRequest request = securityRepository.getAuthenticationRequest(requestId);
        if (request != null) {
            TwoFactorAuthType authType = request.authType();
            if (authType == TwoFactorAuthType.GA && verifyGAKey(request.username(), code)) {
                Authentication authenticate = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(request.username(), request.password()));
                securityRepository.completedRequest(requestId);
                return AuthenticationDetails.token(generateToken(request.username(), authenticate));
            }
        }
        throw new UnauthorizedException("Incorrect two factor params.");
    }

    private String generateToken(String username, Authentication authentication) {
        String token = jwtUtils.generateJwtToken(username);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return token;
    }

    public String generateOtpUrl(String username) {
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials(username);
        return GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL("order-stat.studio", username, key);
    }

    public boolean verifyGAKey(String username, int code) {
        try {
            return googleAuthenticator.authorizeUser(username, code);
        } catch (IllegalArgumentException ignored) {
        }
        return false;
    }
}
