package oss.backend.service;

import oss.backend.domain.Profile;
import oss.backend.repository.core.ProfileRepository;
import oss.backend.security.TwoFactorAuthType;

import javax.annotation.Nullable;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {
    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Nullable
    public Profile getProfileById(long profileId) {
        return profileRepository.getProfileById(profileId);
    }

    public void updateProfileAvatar(long profileId, String data) {
        profileRepository.addProfileAvatar(profileId, data);
    }

    public void deleteProfileAvatar(long profileId) {
        profileRepository.deleteProfileAvatar(profileId);
    }

    public void updatePassword(long profileId, String newPassword) {
        profileRepository.updatePassword(profileId, newPassword);
    }

    public void updateProfile(long profileId, String firstName, String lastName) {
        profileRepository.updateProfile(profileId, firstName, lastName);
    }

    public void updateTwoFactorAuthType(long profileId, TwoFactorAuthType authType) {
        profileRepository.updateTwoFactorAuthType(profileId, authType);
    }

    public boolean isExistEmail(String email) {
        return profileRepository.isExistEmail(email);
    }

    @Nullable
    public Long getNextProfileId() {
        return profileRepository.getNextProfileId();
    }

    public boolean createProfile(long profileId, String firstName, String lastName, String email, String password, long spaceId) {
        return profileRepository.createProfile(profileId, firstName, lastName, email, password, spaceId);
    }
}
