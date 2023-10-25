package oss.backend.controller;

import oss.backend.domain.Profile;
import oss.backend.domain.ApiResponse;
import oss.backend.exception.UnauthorizedException;
import oss.backend.security.TwoFactorAuthType;
import oss.backend.service.ProfileService;
import oss.backend.service.SecurityService;
import oss.backend.util.MappingUtils;
import oss.backend.util.SecurityUtils;

import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResizableByteArrayOutputStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/api/security-settings")
public class SecuritySettingsController {
    private static final String PNG_FORMAT = "PNG";
    private static final int WIDTH = 200;
    private static final int HEIGHT = 200;

    private final ProfileService profileService;
    private final SecurityService securityService;

    public SecuritySettingsController(ProfileService profileService, SecurityService securityService) {
        this.profileService = profileService;
        this.securityService = securityService;
    }

    @GetMapping(path = "/qrcode")
    public ResponseEntity<QRCode> generateQRCode() throws WriterException, IOException {
        Profile profile = getFreshProfile();
        if (profile.authType() == TwoFactorAuthType.GA) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(QRCode.empty());
        }
        String otpProtocol = securityService.generateOtpUrl(profile.username());
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(otpProtocol, BarcodeFormat.QR_CODE, WIDTH, HEIGHT);
        ResizableByteArrayOutputStream byteArrayOutputStream = new ResizableByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, PNG_FORMAT, byteArrayOutputStream);
        return ResponseEntity.ok(QRCode.of(byteArrayOutputStream.toByteArray()));
    }

    @PostMapping(path = "/enable-qrcode")
    public ResponseEntity<ApiResponse> enableQRCode(@RequestBody QRCodeDto code) {
        Profile profile = getFreshProfile();
        if (profile.authType() == TwoFactorAuthType.GA) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.message("Already enabled."));
        }
        if (securityService.verifyGAKey(profile.username(), code.code())) {
            profileService.updateTwoFactorAuthType(profile.id(), TwoFactorAuthType.GA);
            return ResponseEntity.ok(ApiResponse.message("Two factor GA enabled."));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.message("Incorrect verify QR code."));
    }

    @PostMapping(path = "/disable-qrcode")
    public ResponseEntity<ApiResponse> disableQRCode(@RequestBody QRCodeDto code) {
        Profile profile = getFreshProfile();
        if (profile.authType() != TwoFactorAuthType.GA) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.message("Already disabled."));
        }
        if (securityService.verifyGAKey(profile.username(), code.code())) {
            profileService.updateTwoFactorAuthType(profile.id(), TwoFactorAuthType.NONE);
            return ResponseEntity.ok(ApiResponse.message("Two factor GA disabled."));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.message("Incorrect verify QR code."));
    }

    private Profile getFreshProfile() {
        Profile profile = profileService.getProfileById(SecurityUtils.getCurrentUser().getProfileId());
        if (profile == null) {
            throw new UnauthorizedException();
        }
        return profile;
    }

    public static class QRCode {
        private final byte[] qrCode;
        private final String contentType;

        private QRCode(byte[] imageBytes, String contentType) {
            this.qrCode = requireNonNull(imageBytes, "imageBytes can't be null");
            this.contentType = requireNonNull(contentType, "contentType can't be null");
        }

        @JsonSerialize(using = MappingUtils.ByteArraySerializer.class)
        public byte[] getQrCode() {
            return qrCode;
        }

        public String getContentType() {
            return contentType;
        }

        public static QRCode empty() {
            return new QRCode(new byte[0], MediaType.IMAGE_PNG_VALUE);
        }

        public static QRCode of(byte[] imageBytes) {
            return new QRCode(imageBytes, MediaType.IMAGE_PNG_VALUE);
        }
    }

    public record QRCodeDto(@JsonProperty("code") int code) {
    }
}
