package oss.backend.controller;

import oss.backend.domain.dashboard.DashboardPage;
import oss.backend.domain.dashboard.LeadDetailsInfo;
import oss.backend.domain.dashboard.LeadStatusEventInfo;
import oss.backend.domain.dashboard.LeadsItemsWrapper;
import oss.backend.domain.dashboard.LeadsKpiDetails;
import oss.backend.domain.dashboard.LeadsKpiDetailsDay;
import oss.backend.domain.dashboard.ManagerDealsInfo;
import oss.backend.domain.dashboard.PipelineStatusInfoItem;
import oss.backend.service.DashboardService;
import oss.backend.util.DateUtils;
import oss.backend.util.MappingUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private static final Map<Long, LeadsItemsComponentSettings> placeholder = Map.of(
            3340174L,
            LeadsItemsComponentSettings.of(
                    3340174,
                    Set.of(33510130L),
                    Set.of(143L, 34744951L)
            ),
            6502242L,
            LeadsItemsComponentSettings.of(
                    6502242,
                    Set.of(55429186L),
                    Set.of(143L)
            )
    );

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/leads-items")
    public ResponseEntity<LeadsItemsWrapper> getLeadsItemsWrapper(
            @RequestParam String from, @RequestParam String to, @RequestParam long pipelineId
    ) {
        LocalDateTime fromDate = DateUtils.parseFromDate(from, DateUtils.BASE_DATE_FORMATTER);
        LocalDateTime toDate = DateUtils.parseToDate(to, DateUtils.BASE_DATE_FORMATTER);
        LeadsItemsComponentSettings settings = placeholder.get(pipelineId);
        if (fromDate == null || toDate == null || settings == null) {
            return ResponseEntity.ok(LeadsItemsWrapper.empty());
        }

        LeadsItemsWrapper result = dashboardService.getLeadsItemsWrapper(
                fromDate, toDate, settings.pipelineId(), settings.firstChartStatusesId(), settings.secondChartStatusesId()
        );
        //logger.debug("Invoke getLeadsItemsWrapper({}), result: {}.", of, result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/pipeline-info")
    public ResponseEntity<Collection<PipelineStatusInfoItem>> getDashboardPipelineInfo(
            @RequestParam String from, @RequestParam String to, @RequestParam long pipelineId
    ) {
        LocalDateTime fromDate = DateUtils.parseFromDate(from, DateUtils.BASE_DATE_FORMATTER);
        LocalDateTime toDate = DateUtils.parseToDate(to, DateUtils.BASE_DATE_FORMATTER);

        if (fromDate == null || toDate == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        Collection<PipelineStatusInfoItem> result = dashboardService.getDashboardPipelineItems(
                fromDate, toDate, pipelineId
        );
        //logger.debug("Invoke getDashboardPipelineInfo({}, {}, {}), result: {}.", fromDate, toDate, pipelineId, result);
        return ResponseEntity.ok(result);
    }

    @SuppressWarnings("DataFlowIssue")
    @GetMapping("/deals-info")
    public ResponseEntity<Collection<ManagerDealsInfo>> getManagersDealsInfos(
            @RequestParam String from, @RequestParam String to, @RequestParam long pipelineId
    ) {
        LocalDateTime fromDate = DateUtils.parseFromDate(from, DateUtils.BASE_DATE_FORMATTER);
        LocalDateTime toDate = DateUtils.parseToDate(to, DateUtils.BASE_DATE_FORMATTER);

        if (fromDate == null || toDate == null || pipelineId != 6502242) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        Collection<ManagerDealsInfo> result = dashboardService.getManagersDealsInfos(
                fromDate, toDate, 6502242,
                Set.of(55429190L),
                Set.of(55429182L),
                Set.of(142L)
        );
        //logger.debug("Invoke getDashboardPipelineInfo({}), result: {}.", of, result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/status-info")
    public ResponseEntity<Collection<LeadStatusEventInfo>> getLeadStatusInfo(
            @RequestParam String from, @RequestParam String to,
            @RequestParam @Nullable Long pipelineId, @RequestParam @Nullable Long userId
    ) {
        LocalDateTime fromDate = DateUtils.parseFromDate(from, DateUtils.BASE_DATE_FORMATTER);
        LocalDateTime toDate = DateUtils.parseToDate(to, DateUtils.BASE_DATE_FORMATTER);

        if (fromDate == null || toDate == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(dashboardService.getLeadStatusEventsInfos(fromDate, toDate, pipelineId, userId));
    }

    @GetMapping("/leads-kpi-details")
    public ResponseEntity<LeadsKpiDetails> getLeadsKpiDetails(
            @RequestParam String from, @RequestParam String to, @RequestParam long pipelineId
    ) {
        LocalDateTime fromDate = DateUtils.parseFromDate(from, DateUtils.BASE_DATE_FORMATTER);
        LocalDateTime toDate = DateUtils.parseToDate(to, DateUtils.BASE_DATE_FORMATTER);
        LeadsItemsComponentSettings settings = placeholder.get(pipelineId);
        if (fromDate == null || toDate == null || settings == null) {
            return ResponseEntity.ok(LeadsKpiDetails.empty());
        }
        LeadsKpiDetails result = dashboardService.getLeadsKpiDetails(fromDate, toDate, settings.pipelineId(),
                settings.firstChartStatusesId());
        //logger.info("Invoke getLeadsKpiDetails({}, {}, {}), result: {}.", fromDate, toDate, type, result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/leads-kpi-details-day")
    public ResponseEntity<Collection<LeadsKpiDetailsDay>> getLeadsKpiDayDetails(
            @RequestParam String from, @RequestParam String to, @RequestParam long pipelineId
    ) {
        LocalDateTime fromDate = DateUtils.parseFromDate(from, DateUtils.BASE_DATE_FORMATTER);
        LocalDateTime toDate = DateUtils.parseToDate(to, DateUtils.BASE_DATE_FORMATTER);
        LeadsItemsComponentSettings settings = placeholder.get(pipelineId);
        if (fromDate == null || toDate == null || settings == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        Collection<LeadsKpiDetailsDay> result = dashboardService.getLeadsKpiDayDetails(fromDate, toDate,
                settings.pipelineId(), settings.firstChartStatusesId());
        //logger.info("Invoke getLeadsKpiDayDetails({}, {}, {}), result: {}.", fromDate, toDate, type, result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/lead-search")
    public ResponseEntity<DashboardPage<LeadDetailsInfo>> searchLeadItems(
            @RequestParam String from, @RequestParam String to, @RequestParam int page, @RequestParam int pageSize,
            @RequestParam @Nullable String searchValue,
            @RequestParam @Nullable Long pipelineId, @RequestParam @Nullable Long pipelineStatusId
    ) {
        LocalDateTime fromDate = DateUtils.parseFromDate(from, DateUtils.BASE_DATE_FORMATTER);
        LocalDateTime toDate = DateUtils.parseToDate(to, DateUtils.BASE_DATE_FORMATTER);

        if (fromDate == null || toDate == null) {
            return ResponseEntity.ok(DashboardPage.empty());
        }

        return ResponseEntity.ok(dashboardService.getLeadsDetailsPage(fromDate, toDate, page, pageSize, searchValue,
                        pipelineId, pipelineStatusId,
                        DashboardRequestType.getQualifiedStatuses(), DashboardRequestType.getClosedStatuses()));
    }

    public enum DashboardRequestType {
        SPB(
                6502242,
                Set.of(55429186L),
                Set.of(143L),
                Set.of(55429190L),
                Set.of(55429182L),
                Set.of(142L)
        ),
        MSK(
                3340174,
                Set.of(33510130L),
                Set.of(143L, 34744951L),
                Set.of(),
                Set.of(),
                Set.of()
        );

        private final long pipelineId;
        private final Set<Long> qualificationId;
        private final Set<Long> closedId;
        private final Set<Long> completedMeet;
        private final Set<Long> preparedMeet;
        private final Set<Long> completedDeal;

        DashboardRequestType(long pipelineId, Set<Long> qualificationId, Set<Long> closedId,
                Set<Long> completedMeet, Set<Long> preparedMeet, Set<Long> completedDeal) {
            this.pipelineId = pipelineId;
            this.qualificationId = qualificationId;
            this.closedId = closedId;
            this.completedMeet = completedMeet;
            this.preparedMeet = preparedMeet;
            this.completedDeal = completedDeal;
        }

        public long getPipelineId() {
            return pipelineId;
        }

        public Set<Long> getQualificationIds() {
            return qualificationId;
        }

        public Set<Long> getClosedIds() {
            return closedId;
        }

        public Set<Long> getCompletedMeet() {
            return completedMeet;
        }

        public Set<Long> getPreparedMeet() {
            return preparedMeet;
        }

        public Set<Long> getCompletedDeal() {
            return completedDeal;
        }

        public static Set<Long> getQualifiedStatuses() {
            return Stream.concat(
                    SPB.getQualificationIds().stream(),
                    MSK.getQualificationIds().stream()
            ).collect(Collectors.toSet());
        }

        public static Set<Long> getClosedStatuses() {
            return Stream.concat(
                    SPB.getClosedIds().stream(),
                    MSK.getClosedIds().stream()
            ).collect(Collectors.toSet());
        }

        @Nullable
        public static DashboardRequestType of(String value) {
            return Stream.of(values())
                    .filter(dashboardRequestType -> dashboardRequestType.name().equals(value))
                    .findFirst()
                    .orElse(null);
        }
    }

    public record LeadsItemsComponentSettings(
            long pipelineId,
            Set<Long> firstChartStatusesId,
            Set<Long> secondChartStatusesId
    ) {
        @Override
        public String toString() {
            return MappingUtils.convertObjectToJson(this);
        }

        public static LeadsItemsComponentSettings of(
                long pipelineId,
                Set<Long> firstChartStatusesId,
                Set<Long> secondChartStatusesId
        ) {
            return new LeadsItemsComponentSettings(pipelineId, firstChartStatusesId, secondChartStatusesId);
        }
    }
}
