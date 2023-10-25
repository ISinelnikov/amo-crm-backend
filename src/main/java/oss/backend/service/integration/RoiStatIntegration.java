package oss.backend.service.integration;

import oss.backend.domain.roistat.CallDto;
import oss.backend.domain.roistat.CallWrapperDto;
import oss.backend.domain.roistat.Filter;
import oss.backend.domain.roistat.VisitDto;
import oss.backend.domain.roistat.VisitWrapperDto;
import oss.backend.service.ConfigurationService;
import oss.backend.util.HttpUtils;
import oss.backend.util.MappingUtils;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class RoiStatIntegration {
    private static final Logger logger = LoggerFactory.getLogger(RoiStatIntegration.class);

    private static final String VISIT_API_PATH =
            "https://cloud.roistat.com/api/v1/project/site/visit/list?project=%s";

    private static final String CALL_TRACKING_API_PATH =
            "https://cloud.roistat.com/api/v1/project/calltracking/call/list?project=%s";

    private final String projectId;
    private final String apiKey;

    public RoiStatIntegration(ConfigurationService configurationService) {
        this.projectId = configurationService.getApplicationSettings().getRoiStatProjectId();
        this.apiKey = configurationService.getApplicationSettings().getRoiStatApiKey();
    }

    @Nullable
    public VisitDto getVisitByVisitId(long visitId) {
        return getVisitByFilter(Filter.id(visitId));
    }

    @Nullable
    public VisitDto getVisitByLeadId(long leadId) {
        return getVisitByFilter(Filter.orderId(leadId));
    }

    @Nullable
    public VisitDto getVisitByFilter(Filter filter) {
        String preparedUrl = String.format(VISIT_API_PATH, projectId);
        HttpHeaders headers = HttpUtils.getApiKeyHeaders(apiKey);
        ResponseEntity<String> response = HttpUtils.jsonPostRequest(preparedUrl, headers, filter);

        logger.warn("Invoke url: {}, result countryCode: {}, result body: {}.", preparedUrl,
                response.getStatusCode(), response.getBody());

        if (response.getStatusCode().is2xxSuccessful()) {
            VisitWrapperDto visitWrapperDto = MappingUtils.parseJsonToInstance(response.getBody(),
                    VisitWrapperDto.class);
            if (visitWrapperDto != null) {
                return visitWrapperDto.data().stream().findFirst().orElse(null);
            }
        }
        return null;
    }

    @Nullable
    public CallDto getCallByLeadId(long leadId) {
        String preparedUrl = String.format(CALL_TRACKING_API_PATH, projectId);
        HttpHeaders headers = HttpUtils.getApiKeyHeaders(apiKey);

        ResponseEntity<String> response = HttpUtils.jsonPostRequest(preparedUrl, headers, Filter.orderId(leadId));

        logger.trace("Invoke url: {}, result countryCode: {}, result body: {}.", preparedUrl,
                response.getStatusCode(), response.getBody());

        if (response.getStatusCode().is2xxSuccessful()) {
            CallWrapperDto callWrapperDto = MappingUtils.parseJsonToInstance(response.getBody(),
                    CallWrapperDto.class);
            if (callWrapperDto != null) {
                return callWrapperDto.data().stream().findFirst().orElse(null);
            }
        }
        return null;
    }
}
