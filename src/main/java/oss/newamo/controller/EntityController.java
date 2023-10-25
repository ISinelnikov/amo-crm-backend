package oss.newamo.controller;

import oss.backend.domain.space.SpaceSettings;
import oss.backend.exception.BadRequestException;
import oss.backend.exception.InternalServerError;
import oss.backend.service.SpaceService;
import oss.backend.util.SecurityUtils;
import oss.newamo.domain.pipeline.Pipeline;
import oss.newamo.domain.pipeline.status.PipelineStatus;
import oss.newamo.domain.user.User;
import oss.newamo.service.PipelineService;
import oss.newamo.service.UserService;

import java.util.Collection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/entity")
public class EntityController {
    private final PipelineService pipelineService;
    private final UserService userService;
    private final SpaceService spaceService;

    public EntityController(PipelineService pipelineService, UserService userService, SpaceService spaceService) {
        this.pipelineService = pipelineService;
        this.userService = userService;
        this.spaceService = spaceService;
    }

    @GetMapping("/pipelines")
    public ResponseEntity<Collection<Pipeline>> getPipelines() {
        return ResponseEntity.ok(pipelineService.getPipelines(getAmoClientId()));
    }

    @GetMapping("/pipeline-statuses")
    public ResponseEntity<Collection<PipelineStatus>> getPipelineStatuses(@RequestParam long pipelineId) {
        return ResponseEntity.ok(pipelineService.getPipelineStatuses(getAmoClientId(), pipelineId));
    }

    @GetMapping("/users")
    public ResponseEntity<Collection<User>> getUsers() {
        return ResponseEntity.ok(userService.getUsers(getAmoClientId()));
    }

    private String getAmoClientId() {
        long spaceId = SecurityUtils.getCurrentUser().getProfile().spaceId();
        SpaceSettings space = spaceService.getSpaceSettings(spaceId);
        if (space == null) {
            throw new InternalServerError("space not found");
        }
        String clientId = space.amoClientId();
        if (clientId == null) {
            throw  new BadRequestException("clientId not found");
        }
        return clientId;
    }
}
