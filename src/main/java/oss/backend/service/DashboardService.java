package oss.backend.service;

import oss.oldamo.repository.LeadSearchRepository;
import oss.backend.domain.dashboard.DashboardPage;
import oss.backend.domain.dashboard.LeadDetailsInfo;
import oss.backend.domain.dashboard.LeadStatusEventInfo;
import oss.backend.domain.dashboard.LeadsItemsWrapper;
import oss.backend.domain.dashboard.LeadsKpiDetails;
import oss.backend.domain.dashboard.LeadsKpiDetailsDay;
import oss.backend.domain.dashboard.ManagerDealsInfo;
import oss.backend.domain.dashboard.PipelineStatusInfoItem;
import oss.backend.repository.DashboardRepository;
import oss.newamo.service.UserService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final UserService userService;
    private final DashboardRepository dashboardRepository;
    private final LeadSearchRepository leadSearchRepository;

    public DashboardService(UserService userService, DashboardRepository dashboardRepository,
            LeadSearchRepository leadSearchRepository) {
        this.userService = userService;
        this.dashboardRepository = dashboardRepository;
        this.leadSearchRepository = leadSearchRepository;
    }

    public LeadsKpiDetails getLeadsKpiDetails(
            LocalDateTime fromDate, LocalDateTime toDate, long pipelineId, Set<Long> qualified
    ) {
        return dashboardRepository.getLeadsKpiDetails(fromDate, toDate, pipelineId, qualified);
    }

    public LeadsItemsWrapper getLeadsItemsWrapper(
            LocalDateTime fromDate, LocalDateTime toDate, long pipelineId, Set<Long> qualificationIds, Set<Long> closedIds
    ) {
        return LeadsItemsWrapper.of(
                dashboardRepository.getAllLeadItems(fromDate, toDate, pipelineId),
                dashboardRepository.getLeadSourceItemsByStatusIds(fromDate, toDate, pipelineId, qualificationIds),
                dashboardRepository.getLeadSourceItemsByStatusIds(fromDate, toDate, pipelineId, closedIds)
        );
    }

    public Collection<PipelineStatusInfoItem> getDashboardPipelineItems(LocalDateTime fromDate, LocalDateTime toDate, long pipelineId) {
        return dashboardRepository.getDashboardPipelineInfoItems(fromDate, toDate, pipelineId);
    }

    public Collection<ManagerDealsInfo> getManagersDealsInfos(
            LocalDateTime fromDate, LocalDateTime toDate, long pipelineId,
            Set<Long> completedMeetIds, Set<Long> preparedMeetIds, Set<Long> completedDealIds) {
        return userService.getUsers("a734bcaf-419e-4bcb-a102-aad3557c3e70")
                .stream()
                .map(manager -> {
                    long id = manager.id();
                    return new ManagerDealsInfo(
                            manager.name(),
                            dashboardRepository.getLeadsCountByManagerId(fromDate, toDate, pipelineId, completedMeetIds, id),
                            dashboardRepository.getLeadsCountByManagerId(fromDate, toDate, pipelineId, preparedMeetIds, id),
                            dashboardRepository.getLeadsCountByManagerId(fromDate, toDate, pipelineId, completedDealIds, id),
                            dashboardRepository.getRevenueByManagerId(fromDate, toDate, pipelineId, completedDealIds, id)
                    );
                })
                .collect(Collectors.toList());
    }

    public Collection<LeadStatusEventInfo> getLeadStatusEventsInfos(
            LocalDateTime fromDate, LocalDateTime toDate, @Nullable Long pipelineId, @Nullable Long userId
    ) {
        return dashboardRepository.getLeadStatusEventsInfos(fromDate, toDate, pipelineId, userId);
    }

    public DashboardPage<LeadDetailsInfo> getLeadsDetailsPage(
            LocalDateTime fromDate, LocalDateTime toDate, int page, int pageSize,
            @Nullable String searchValue, @Nullable Long pipelineId, @Nullable Long pipelineStatusId,
            Set<Long> qualifiedIds, Set<Long> closedIds
    ) {
        return leadSearchRepository.getLeadsDetailsPage(fromDate, toDate, page, pageSize,
                searchValue, pipelineId, pipelineStatusId, qualifiedIds, closedIds);
    }

    public Collection<LeadsKpiDetailsDay> getLeadsKpiDayDetails(
            LocalDateTime fromDate, LocalDateTime toDate, long pipelineId, Set<Long> qualifiedIds
    ) {
        return dashboardRepository.getLeadsKpiDayDetails(fromDate, toDate, pipelineId, qualifiedIds);
    }
}
