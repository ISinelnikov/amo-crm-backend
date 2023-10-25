package oss.bot.controller;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import oss.backend.domain.ApiResponse;
import oss.bot.ApiBotAuthResponse;
import oss.bot.BotHelpService;
import oss.bot.BotReferrer;
import oss.bot.BotUserDetails;
import oss.bot.ExternalLead;
import oss.bot.dto.AddLeadDto;
import oss.bot.dto.SignInDto;
import oss.bot.dto.SignUpDto;
import oss.bot.dto.ValidateReferralCodeDto;
import oss.bot.dto.ValidateSessionDto;
import oss.backend.exception.UnauthorizedException;
import oss.backend.service.ConfigurationService;
import oss.backend.util.RequestUtils;
import oss.backend.util.SequenceUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/bot-help")
public class BotHelpController {
    private static final Logger logger = LoggerFactory.getLogger(BotHelpController.class);

    private static final Map<String, Long> reportIdToUserId = new ConcurrentHashMap<>();

    private static final String INCORRECT_INTEGRATION_TOKEN = "Интеграция настроена не корректно";

    private static final ApiBotAuthResponse INCORRECT_INTEGRATION_TOKEN_RESPONSE = ApiBotAuthResponse.internalError(
            ApiBotAuthResponse.ApiBothAuthMessage.of(INCORRECT_INTEGRATION_TOKEN)
    );

    private static final String INCORRECT_REFERRAL_CODE = "Некорректный пригласительный код";

    private static final ApiBotAuthResponse INCORRECT_REFERRAL_CODE_RESPONSE = ApiBotAuthResponse.internalError(
            ApiBotAuthResponse.ApiBothAuthMessage.of(INCORRECT_REFERRAL_CODE)
    );

    private static final String USER_ALREADY_EXIST = "Пользователь уже зарегистрирован";

    private static final ApiBotAuthResponse USER_ALREADY_EXIST_RESPONSE = ApiBotAuthResponse.internalError(
            ApiBotAuthResponse.ApiBothAuthMessage.of(USER_ALREADY_EXIST)
    );

    private static final String SESSION_EXIST = "Пользователь найден";

    private static final ApiBotAuthResponse AUTHORIZED_RESPONSE = ApiBotAuthResponse.authorized(
            ApiBotAuthResponse.ApiBothAuthMessage.of(SESSION_EXIST)
    );

    private static final String SESSION_NOT_EXIST = "Пользователь не найден";

    private static final ApiBotAuthResponse UNAUTHORIZED_RESPONSE = ApiBotAuthResponse.unauthorized(
            ApiBotAuthResponse.ApiBothAuthMessage.of(SESSION_NOT_EXIST)
    );

    private final BotHelpService botHelpService;
    private final String addLeadBotToken;

    public BotHelpController(BotHelpService botHelpService, ConfigurationService configurationService) {
        this.botHelpService = botHelpService;
        this.addLeadBotToken = configurationService.getApplicationSettings().getBotHelp().getAddLeadBotToken();
    }

    @PostMapping("/validate-session")
    public ResponseEntity<ApiBotAuthResponse> validateSession(@RequestBody ValidateSessionDto validateSessionDto,
            @RequestParam String userId) {
        logger.debug("Invoke validateSession({}), userId: {}.", validateSessionDto, userId);
        if (isValidAddLeadBotToken(validateSessionDto.token())) {
            boolean existSession = botHelpService.isExistSessionByBotHelpUserId(userId);
            logger.debug("Invoke isExistSessionByBotHelpUserId({}), result: {}.", userId, existSession);
            return ResponseEntity.ok(
                    existSession ? AUTHORIZED_RESPONSE : UNAUTHORIZED_RESPONSE
            );
        }
        return ResponseEntity.ok(INCORRECT_INTEGRATION_TOKEN_RESPONSE);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<ApiBotAuthResponse> signIn(@RequestBody SignInDto signInDto, @RequestParam String userId) {
        logger.debug("Invoke signIn({}), userId: {}.", signInDto, userId);
        if (isValidAddLeadBotToken(signInDto.token())) {
            return ResponseEntity.ok(
                    botHelpService.signIn(signInDto.accessToken(), userId) ? AUTHORIZED_RESPONSE : UNAUTHORIZED_RESPONSE
            );
        }
        return ResponseEntity.ok(INCORRECT_INTEGRATION_TOKEN_RESPONSE);
    }

    @PostMapping("/validate-referral-code")
    public ResponseEntity<ApiBotAuthResponse> validateReferralCode(@RequestBody ValidateReferralCodeDto validateReferralCodeDto) {
        if (isValidAddLeadBotToken(validateReferralCodeDto.token())) {
            Long referrerId = botHelpService.getUserIdByReferralCode(validateReferralCodeDto.referralCode());
            if (referrerId == null) {
                return ResponseEntity.ok(INCORRECT_REFERRAL_CODE_RESPONSE);
            } else {
                return ResponseEntity.ok(ApiBotAuthResponse.correctReferrerCode(
                        ApiBotAuthResponse.ApiBothAuthMessage.of("Пригласительный код найден.")
                ));
            }
        }
        return ResponseEntity.ok(INCORRECT_INTEGRATION_TOKEN_RESPONSE);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<ApiBotAuthResponse> signUp(@RequestBody SignUpDto signUpDto, @RequestParam String userId) {
        logger.debug("Invoke signUp({}), userId: {}.", signUpDto, userId);
        if (isValidAddLeadBotToken(signUpDto.token())) {
            if (botHelpService.isExistSessionByBotHelpUserId(userId)) {
                return ResponseEntity.ok(USER_ALREADY_EXIST_RESPONSE);
            }
            Long referrerId = botHelpService.getUserIdByReferralCode(signUpDto.referralCode());
            if (referrerId == null) {
                return ResponseEntity.ok(INCORRECT_REFERRAL_CODE_RESPONSE);
            }
            if (botHelpService.signUp(referrerId, signUpDto.name(), signUpDto.phone(), userId)) {
                return ResponseEntity.ok(ApiBotAuthResponse.authorized(
                        ApiBotAuthResponse.ApiBothAuthMessage.of("Пользователь успешно зарегистирован.")
                ));
            } else {
                return ResponseEntity.ok(ApiBotAuthResponse.internalError(
                        ApiBotAuthResponse.ApiBothAuthMessage.of("Во время регистрации пользователя возникла ошибка.")
                ));
            }
        }
        return ResponseEntity.ok(INCORRECT_INTEGRATION_TOKEN_RESPONSE);
    }

    @PostMapping("/profile")
    public ResponseEntity<ApiBotAuthResponse> profile(@RequestBody ValidateSessionDto validateSessionDto, @RequestParam String userId) {
        logger.debug("Invoke profile({}), userId: {}.", validateSessionDto, userId);
        if (isValidAddLeadBotToken(validateSessionDto.token())) {
            BotUserDetails userDetails = botHelpService.getBotUserDetailsById(userId);
            if (userDetails == null) {
                return ResponseEntity.ok(UNAUTHORIZED_RESPONSE);
            } else {
                return ResponseEntity.ok(ApiBotAuthResponse.authorized(userDetails));
            }
        }
        return ResponseEntity.ok(INCORRECT_INTEGRATION_TOKEN_RESPONSE);
    }

    @PostMapping("/add-lead")
    public ResponseEntity<ApiBotAuthResponse> addLead(@RequestBody AddLeadDto leadDto, @RequestParam String userId) {
        logger.debug("Invoke addLeadInfo({}), userId: {}.", leadDto, userId);
        if (isValidAddLeadBotToken(leadDto.token())) {
            if (botHelpService.isExistSessionByBotHelpUserId(userId)) {
                botHelpService.processLeadInfo(
                        userId, leadDto.clientName(), leadDto.clientPhone(), leadDto.comment()
                );
                return ResponseEntity.ok(ApiBotAuthResponse.leadCreated());
            } else {
                return ResponseEntity.ok(UNAUTHORIZED_RESPONSE);
            }
        }
        return ResponseEntity.ok(INCORRECT_INTEGRATION_TOKEN_RESPONSE);
    }

    @PostMapping(value = "/referral-url")
    public ResponseEntity<ApiBotAuthResponse> getReportLink(@RequestBody ValidateSessionDto validateSessionDto, @RequestParam String userId) {
        logger.debug("Invoke getReportId({}), userId: {}.", validateSessionDto, userId);
        if (isValidAddLeadBotToken(validateSessionDto.token())) {
            BotUserDetails userDetails = botHelpService.getBotUserDetailsById(userId);
            if (userDetails == null) {
                return ResponseEntity.ok(UNAUTHORIZED_RESPONSE);
            } else {
                String reportId = SequenceUtils.getCharactersToken(10);
                reportIdToUserId.put(reportId, userDetails.userId());
                String reportUrl = RequestUtils.getCurrentPath() + "/api/bot-help/referral-page?id=" + reportId;
                logger.info("Invoke getReportLink({}), url: {}.", userId, reportUrl);
                return ResponseEntity.ok(ApiBotAuthResponse.authorized(ApiResponse.reportUrl(reportUrl)));
            }
        }
        return ResponseEntity.ok(INCORRECT_INTEGRATION_TOKEN_RESPONSE);
    }

    @GetMapping(value = "/referral-page", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String getReferralReport(@RequestParam String id) throws IOException {
        Long userId = 5L;//reportIdToUserId.get(id);

        if (userId == null) {
            logger.info("Invoke getReferralReport({}), userId not found.", id);
            throw new UnauthorizedException();
        }

        Collection<BotReferrer> referrerLeads = botHelpService.getMyReferrerLeads(userId);
        Collection<ExternalLead> myLeads = botHelpService.getMyLeads(userId);
        Collection<ExternalLead> byStatus = Collections.emptyList();
        if (userId == 5) {
            byStatus = botHelpService.getByStatus(1091617);
        }

        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache m = mf.compile("referral.html");
        StringWriter writer = new StringWriter();
        m.execute(writer, Map.of(
                "emptyLeads", referrerLeads.isEmpty() && myLeads.isEmpty(),
                "referrerLeads", referrerLeads,
                "myLeads", myLeads,
                "byStatus", byStatus
        )).flush();
        return writer.toString();
    }

    private boolean isValidAddLeadBotToken(String token) {
        return addLeadBotToken.equals(token);
    }
}
