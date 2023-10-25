package oss.backend.service.integration;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import oss.oldamo.service.LeadService;
import oss.backend.configuration.ThreadScopeConfiguration;
import oss.oldamo.domain.api.LeadModel;
import oss.backend.domain.roistat.CallDto;
import oss.backend.domain.roistat.VisitDto;
import oss.backend.domain.source.CallSourcePattern;
import oss.backend.domain.source.GroupInfo;
import oss.backend.domain.source.VisitSourcePattern;
import oss.backend.exception.LeadNotFound;
import oss.backend.repository.SourceRepository;
import oss.backend.util.CustomFieldUtils;
import oss.backend.util.NumberUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static oss.oldamo.service.LeadService.SOURCE_SELECT_ID;

@Service
public class SourceService {
    private static final Logger logger = LoggerFactory.getLogger(SourceService.class);

    public static long ROI_STAT_TRACKING_DATA = 518753L;

    private final RoiStatIntegration roiStatIntegration;
    private final LeadService leadService;

    private final Collection<VisitSourcePattern> visitSourcePatterns;
    private final Collection<CallSourcePattern> callSourcePatterns;

    private final SourceRepository sourceRepository;

    public SourceService(RoiStatIntegration roiStatIntegration, LeadService leadService, SourceRepository sourceRepository,
                         @Qualifier(ThreadScopeConfiguration.DEFAULT_SCHEDULER) ScheduledExecutorService scheduledExecutorService) {
        this.roiStatIntegration = roiStatIntegration;
        this.leadService = leadService;
        this.visitSourcePatterns = sourceRepository.getVisitSourcePatterns();
        this.callSourcePatterns = sourceRepository.getCallSourcePatterns();
        this.sourceRepository = sourceRepository;

        scheduledExecutorService.scheduleWithFixedDelay(this::refreshUnknownSources, 0,
                15L, TimeUnit.MINUTES);
    }

    public void refreshUnknownSources() {
        logger.info("start refreshUnknownSources()");
        Set<Long> leads = sourceRepository.getCurrentSourcesLeads();
        leads.forEach(lead -> {
            try {
                findSource(lead);
            } catch (LeadNotFound ignored) {
            }
        });
        logger.info("finish refreshUnknownSources()");
    }

    private LeadModel getModel(long leadId) throws LeadNotFound {
        LeadModel lead = leadService.getLeadById(leadId).orElse(null);
        if (lead == null) {
            logger.error("Not found lead: {} in amoCRM, mark as delete.", leadId);
            //leadService.deleteLead(leadId);
            throw LeadNotFound.of(leadId);
        }
        return lead;
    }

    public void findSource(long leadId) throws LeadNotFound {
        logger.info("Lead: {}", leadId);
        LeadModel lead = getModel(leadId);

        Long optionId = CustomFieldUtils.getFirstValueId(lead.getFields().get(SOURCE_SELECT_ID));

        if (optionId != null) {
            logger.info("Lead: {}, option: {}, select: {}", leadId, optionId, SOURCE_SELECT_ID);
            leadService.changeLeadSourceId(leadId, optionId, SOURCE_SELECT_ID);
        }

        String firstValue = CustomFieldUtils.getFirstValue(
                lead.getFields().get(ROI_STAT_TRACKING_DATA)
        );
        Long visitId = NumberUtils.longOrNull(firstValue);
        if (visitId != null) {
            logger.info("Found visitId: {} for leadId: {}", visitId, leadId);
            VisitDto visitByVisitId = getVisitByVisitId(visitId);
            Collection<Pair<Long, Long>> details = getLeadSourceDetailsByVisit(visitByVisitId, leadId);
            if (visitByVisitId != null && !details.isEmpty()) {
                for (Pair<Long, Long> detail : details) {
                    updateLeadByGroupId(detail.getLeft(), detail.getRight());
                }
                return;
            }
        }
        Long groupIdByCall = getLeadSourceGroupIdByCall(leadId);
        if (groupIdByCall != null) {
            updateLeadByGroupId(leadId, groupIdByCall);
            return;
        }

        processLeadName(leadId);
    }

    private void processLeadName(long leadId) throws LeadNotFound {
        LeadModel lead = getModel(leadId);
        for (CallSourcePattern callSourcePattern : callSourcePatterns) {
            if (lead.getName().contains(callSourcePattern.phoneNumber())) {
                logger.info("Find call pattern: {} in lead: {} header.", callSourcePattern, leadId);
                updateLeadByGroupId(leadId, callSourcePattern.groupId());
                break;
            }
        }
    }

    private void updateLeadByGroupId(long leadId, long groupId) {
        try {
            getModel(leadId);

            GroupInfo info = sourceRepository.getGroupInfoById(groupId);
            if (info == null) {
                logger.error("Not found group: {}, lead: {}.", groupId, leadId);
            } else {
                logger.info("Lead: {}, option: {}, select: {}", leadId, info.optionId(), info.selectId());
                leadService.changeLeadSourceId(leadId, info.optionId(), info.selectId());
            }
        } catch (LeadNotFound ex) {
            logger.warn("Lead already deleted: {}.", leadId);
            leadService.deleteLead(leadId);
        }
    }

    @Nullable
    private Long getLeadSourceGroupIdByCall(long leadId) {
        CallDto call = roiStatIntegration.getCallByLeadId(leadId);
        if (call != null && call.getId() != null && call.getCallee() != null) {
            String callee = call.getCallee();
            CallSourcePattern sourceInfo = callSourcePatterns
                    .stream()
                    .filter(callSourcePattern -> callSourcePattern.fullNumber().equals(callee))
                    .findFirst()
                    .orElse(null);

            if (sourceInfo != null) {
                return sourceInfo.groupId();
            } else {
                logger.warn("Not found CalleeSourceInfo by callee: {}, lead id: {}.", callee, leadId);
            }
        }
        return null;
    }

    private VisitDto getVisitByLeadId(long leadId) {
        return roiStatIntegration.getVisitByLeadId(leadId);
    }

    private VisitDto getVisitByVisitId(long visitId) {
        return roiStatIntegration.getVisitByVisitId(visitId);
    }

    private Collection<Pair<Long, Long>> getLeadSourceDetailsByVisit(VisitDto visit, long leadId) {
        if (visit != null && visit.getSource() != null) {
            List<String> displayNameByLevel = visit.getSource().displayNameByLevel();
            List<VisitSourcePattern> result = visitSourcePatterns.stream()
                    .filter(visitSourcePattern -> visitSourcePattern.matchVisit(displayNameByLevel))
                    .toList();

            if (result.isEmpty()) {
                logger.error("Invoke getLeadSourceDetailsByVisit({}, {}), patterns not found.", visit, leadId);
                return Collections.emptyList();
            }

            VisitSourcePattern topPriorityResult = Collections.max(result,
                    Comparator.comparing(VisitSourcePattern::getPriority));

            logger.info("Order: {}, marker: {}, result: {}, other: {}.", visit, displayNameByLevel, topPriorityResult, result);

            if (topPriorityResult == null) {
                return Collections.emptyList();
            }

            Set<Long> orderIds = visit.getOrderIds();
            if (!orderIds.contains(leadId)) {
                logger.error("Not found lead id: {} in visit: {}", leadId, visit.getId());
            }
            return orderIds
                    .stream()
                    .map(id -> Pair.of(id, topPriorityResult.getGroupId()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
