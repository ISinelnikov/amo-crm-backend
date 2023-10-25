package oss.backend.controller;

import oss.backend.domain.Profile;
import oss.backend.domain.ApiResponse;
import oss.backend.security.User;
import oss.backend.service.ProfileService;
import oss.backend.util.OSSStringUtils;
import oss.backend.util.SecurityUtils;

import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<Profile> getCurrentProfile() {
        return ResponseEntity.ok(profileService.getProfileById(SecurityUtils.getCurrentUser().getProfileId()));
    }

    @PutMapping
    public ResponseEntity<ApiResponse> updateProfile(@RequestBody UpdateProfileDto updateProfileDto) {
        profileService.updateProfile(SecurityUtils.getCurrentUser().getProfileId(),
                updateProfileDto.firstName(), updateProfileDto.lastName());
        return ResponseEntity.ok(ApiResponse.empty());
    }

    @PutMapping("/avatar")
    public ResponseEntity<ApiResponse> updateAvatar(@RequestBody Base64Wrapper base64) {
        profileService.updateProfileAvatar(
                SecurityUtils.getCurrentUser().getProfileId(),
                base64.data()
        );
        return ResponseEntity.ok(ApiResponse.empty());
    }

    @DeleteMapping("/avatar")
    public ResponseEntity<ApiResponse> deleteAvatar() {
        profileService.deleteProfileAvatar(SecurityUtils.getCurrentUser().getProfileId());
        return ResponseEntity.ok(ApiResponse.empty());
    }

    @PutMapping(path = "/update-password")
    public ResponseEntity<ApiResponse> updatePassword(@RequestBody UpdatePasswordDto updatePasswordDto) {
        User user = SecurityUtils.getCurrentUser();
        if (user.getProfile().password().equals(updatePasswordDto.currentPassword())) {
            profileService.updatePassword(user.getProfileId(), updatePasswordDto.newPassword());
            return ResponseEntity.ok(ApiResponse.empty());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.message("Incorrect current password."));
    }

    public record UpdateProfileDto(String firstName, String lastName) {
        @JsonCreator
        public UpdateProfileDto(
                @JsonProperty("firstName") String firstName,
                @JsonProperty("lastName") String lastName
        ) {
            this.firstName = Objects.requireNonNull(
                    OSSStringUtils.valueToNull(firstName),
                    "firstName can't be null"
            );
            this.lastName = Objects.requireNonNull(
                    OSSStringUtils.valueToNull(lastName),
                    "lastName can't be null"
            );
        }
    }

    public record UpdatePasswordDto(String currentPassword, String newPassword) {
        @JsonCreator
        public UpdatePasswordDto(
                @JsonProperty("currentPassword") String currentPassword,
                @JsonProperty("newPassword") String newPassword
        ) {
            this.currentPassword = Objects.requireNonNull(
                    OSSStringUtils.valueToNull(currentPassword),
                    "currentPassword can't be null"
            );
            this.newPassword = Objects.requireNonNull(
                    OSSStringUtils.valueToNull(newPassword),
                    "newPassword can't be null"
            );
        }
    }

    public record Base64Wrapper(@JsonProperty("base64") String data) {
    }
}
