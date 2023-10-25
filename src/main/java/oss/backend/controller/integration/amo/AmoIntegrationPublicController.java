package oss.backend.controller.integration.amo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import oss.backend.util.RequestUtils;
import oss.newamo.service.ClientConnectionService;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping(path = "${integration.public}" + AmoIntegration.AMO_PATH)
public class AmoIntegrationPublicController extends AmoIntegration {
    private static final Logger logger = LoggerFactory.getLogger(AmoIntegrationPublicController.class);

    private final ClientConnectionService clientConnectionService;

    public AmoIntegrationPublicController(ClientConnectionService clientConnectionService) {
        this.clientConnectionService = clientConnectionService;
    }

    @GetMapping(value = LOGO_PATH, produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getIntegrationLogo() throws IOException {
        var integrationLogo = new ClassPathResource("amo-integration-logo.jpg");
        byte[] bytes = StreamUtils.copyToByteArray(integrationLogo.getInputStream());
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
    }

    @PostMapping(CONNECT_PATH)
    public ResponseEntity<String> connect(@RequestBody ConnectDto connectDto) {
        logger.info("Invoke connect({}), nginx headers: {}", connectDto,
                RequestUtils.getCurrentRequestNginxHeaders());
        clientConnectionService.updateClientConnectionSecret(connectDto.operationId(), connectDto.clientId(), connectDto.clientSecret());
        return ResponseEntity.ok("{}");
    }

    @GetMapping(REDIRECT_PATH)
    public RedirectView redirect(
            @RequestParam @Nullable String code,
            @RequestParam("state") @Nullable String operationId,
            @RequestParam @Nullable String referer,
            @RequestParam("client_id") @Nullable String clientId
    ) {
        clientConnectionService.updateClientConnectionCode(operationId, code, referer, clientId);
        logger.info("Invoke redirect({}, {}, {}, {}).", code, operationId, referer, clientId);
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(RequestUtils.getCurrentPath());
        return redirectView;
    }

    public record ConnectDto(String clientId, String clientSecret, String operationId) {
        @JsonCreator
        public ConnectDto(
                @JsonProperty("client_id") String clientId,
                @JsonProperty("client_secret") String clientSecret,
                @JsonProperty("state") String operationId
        ) {
            this.clientId = Objects.requireNonNull(clientId, "clientId can't be null.");
            this.clientSecret = Objects.requireNonNull(clientSecret, "clientSecret can't be null.");
            this.operationId = Objects.requireNonNull(operationId, "operationId can't be null.");
        }
    }
}
