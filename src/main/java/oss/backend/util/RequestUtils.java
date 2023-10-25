package oss.backend.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Nullable;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class RequestUtils {
    public static final String HTTPS = "https://";

    private RequestUtils() {
    }

    public static Map<NginxHeader, String> getCurrentRequestNginxHeaders() {
        HttpServletRequest request = getCurrentRequest();
        return NginxHeader.allHeaders()
                .stream()
                .map(nginxHeader -> {
                    String requestHeader = request.getHeader(nginxHeader.getHeader());
                    if (StringUtils.hasText(requestHeader)) {
                        return new AbstractMap.SimpleEntry<>(nginxHeader, requestHeader);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    @Nullable
    public static String getCurrentPath() {
        HttpServletRequest request = getCurrentRequest();
        String protoHeader = request.getHeader(NginxHeader.FORWARDED_PROTO.getHeader());
        String host = request.getHeader(NginxHeader.HOST.getHeader());
        if (StringUtils.hasText(protoHeader) && StringUtils.hasText(host)) {
            return String.format("%s://%s", protoHeader, host);
        }
        return null;
    }

    @Nullable
    public static String getCurrentHost() {
        return getCurrentRequest().getHeader(NginxHeader.HOST.getHeader());
    }

    private static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return servletRequestAttributes.getRequest();
    }

    public enum NginxHeader {
        REQUEST_URI("Request-URI"),
        HOST("Host"),
        REAL_IP("X-Real-IP"),
        FORWARDED_FOR("X-Forwarded-For"),
        FORWARDED_HOST("X-Forwarded-Host"),
        FORWARDED_SERVER("X-Forwarded-Server"),
        FORWARDED_PORT("X-Forwarded-Port"),
        FORWARDED_PROTO("X-Forwarded-Proto");

        private final String header;

        NginxHeader(String header) {
            this.header = header;
        }

        public String getHeader() {
            return header;
        }

        public static Set<NginxHeader> allHeaders() {
            return Arrays.stream(values()).collect(Collectors.toSet());
        }
    }
}
