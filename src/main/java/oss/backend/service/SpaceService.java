package oss.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import oss.backend.domain.space.SpaceSettings;
import oss.backend.repository.SpaceRepository;
import oss.backend.util.HttpUtils;

import javax.annotation.Nullable;

@Service
public class SpaceService {
    private final Logger logger = LoggerFactory.getLogger(SpaceService.class);

    private final ProfileService profileService;
    private final SpaceRepository spaceRepository;
    private final EmailService emailService;

    public SpaceService(ProfileService profileService, SpaceRepository spaceRepository, EmailService emailService) {
        this.profileService = profileService;
        this.spaceRepository = spaceRepository;
        this.emailService = emailService;
    }

    @Nullable
    public SpaceSettings getSpaceSettings(long spaceId) {
        return spaceRepository.getSpaceSettings(spaceId);
    }

    public boolean isExistEmail(String email) {
        return profileService.isExistEmail(email);
    }

    public void createSpaceWithOwner(
            String spaceName,
            String firstName, String lastName,
            String email, String password
    ) {
        Long profileId = profileService.getNextProfileId();
        Long spaceId = spaceRepository.getNextSpaceId();

        if (profileId == null || spaceId == null) {
            logger.error("Invoke createSpaceWithOwner(..{}..), profileId or spaceId is null.", email);
            return;
        }

        if (spaceRepository.createSpace(spaceId, spaceName)
                && profileService.createProfile(profileId, firstName, lastName, email, password, spaceId)) {
            logger.debug("Created spaceId: {}, spaceName: {}, profileId: {}, email: {}.",
                    spaceId, spaceName, profileId, email);
            emailService.sendConfirmationEmail(email);
        }
    }
}
