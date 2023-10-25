package oss.backend.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import oss.backend.domain.ApiResponse;
import oss.backend.service.SpaceService;

import static java.util.Objects.requireNonNull;
import static oss.backend.util.OSSStringUtils.valueToNull;

@RestController
@RequestMapping("/api/space-registration")
public class SpaceRegistrationController {
    private static final Logger logger = LoggerFactory.getLogger(SpaceRegistrationController.class);

    private final SpaceService spaceService;

    public SpaceRegistrationController(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> spaceRegistration(@RequestBody SpaceRegistrationDto registrationDto) {
        logger.debug("Invoke whiteLabelRegistration({}).", registrationDto);
        if (spaceService.isExistEmail(registrationDto.email())) {
            return ResponseEntity.badRequest().body(ApiResponse.details("Email already exist."));
        }
        spaceService.createSpaceWithOwner(
                registrationDto.spaceName(),
                registrationDto.firstName(), registrationDto.lastName(),
                registrationDto.email(), registrationDto.password()
        );
        return ResponseEntity.ok(ApiResponse.details("Confirm email for enabling account."));
    }

    public record SpaceRegistrationDto(
            String spaceName, String firstName, String lastName,
            String email, String password
    ) {
        @JsonCreator
        public SpaceRegistrationDto(
                @JsonProperty("spaceName") String spaceName,
                @JsonProperty("firstName") String firstName,
                @JsonProperty("lastName") String lastName,
                @JsonProperty("email") String email,
                @JsonProperty("password") String password
        ) {
            this.spaceName = requireNonNull(valueToNull(spaceName), "spaceName can't be null.");
            this.firstName = requireNonNull(valueToNull(firstName), "firstName can't be null.");
            this.lastName = requireNonNull(valueToNull(lastName), "lastName can't be null.");
            this.email = requireNonNull(valueToNull(email), "email can't be null.");
            this.password = requireNonNull(valueToNull(password), "password can't be null.");
        }
    }
}
