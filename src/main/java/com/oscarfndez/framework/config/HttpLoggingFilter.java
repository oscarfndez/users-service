package com.oscarfndez.framework.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpLoggingFilter extends OncePerRequestFilter {

    private static final int MAX_PAYLOAD_LENGTH = 4_000;
    private static final Set<String> SENSITIVE_HEADERS = Set.of(
            "authorization",
            "cookie",
            "set-cookie",
            "x-api-key"
    );
    private static final Pattern SENSITIVE_JSON_FIELDS = Pattern.compile(
            "(?i)(\"(?:password|token|accessToken|refreshToken|secret)\"\\s*:\\s*\")([^\"]*)(\")"
    );

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (!log.isDebugEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        long startedAt = System.currentTimeMillis();

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            logRequestAndResponse(wrappedRequest, wrappedResponse, System.currentTimeMillis() - startedAt);
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequestAndResponse(ContentCachingRequestWrapper request,
            ContentCachingResponseWrapper response,
            long durationMs) {
        String path = request.getRequestURI() + (request.getQueryString() == null ? "" : "?" + request.getQueryString());
        log.debug("HTTP {} {} -> {} ({} ms)", request.getMethod(), path, response.getStatus(), durationMs);

        if (!log.isTraceEnabled()) {
            return;
        }

        log.trace("HTTP request headers method={} path={} headers={}",
                request.getMethod(), path, safeHeaders(request));
        log.trace("HTTP request body method={} path={} body={}",
                request.getMethod(), path, readPayload(request.getContentAsByteArray(), request.getCharacterEncoding(), request.getContentType()));
        log.trace("HTTP response headers method={} path={} status={} headers={}",
                request.getMethod(), path, response.getStatus(), safeHeaders(response));
        log.trace("HTTP response body method={} path={} status={} body={}",
                request.getMethod(), path, response.getStatus(), readPayload(response.getContentAsByteArray(), response.getCharacterEncoding(), response.getContentType()));
    }

    private Map<String, List<String>> safeHeaders(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(
                        header -> header,
                        header -> Collections.list(request.getHeaders(header))
                                .stream()
                                .map(value -> safeHeaderValue(header, value))
                                .toList(),
                        (existing, duplicate) -> existing
                ));
    }

    private Map<String, List<String>> safeHeaders(HttpServletResponse response) {
        return response.getHeaderNames()
                .stream()
                .collect(Collectors.toMap(
                        header -> header,
                        header -> response.getHeaders(header)
                                .stream()
                                .map(value -> safeHeaderValue(header, value))
                                .toList(),
                        (existing, duplicate) -> existing
                ));
    }

    private String safeHeaderValue(String header, String value) {
        if (header != null && SENSITIVE_HEADERS.contains(header.toLowerCase(Locale.ROOT))) {
            return "<masked>";
        }

        return value;
    }

    private String readPayload(byte[] content, String encoding, String contentType) {
        if (content.length == 0) {
            return "";
        }
        if (!isVisibleContent(contentType)) {
            return "<binary or unsupported content>";
        }

        try {
            String payload = new String(content, encoding == null ? "UTF-8" : encoding);
            if (payload.length() > MAX_PAYLOAD_LENGTH) {
                payload = payload.substring(0, MAX_PAYLOAD_LENGTH) + "...<truncated>";
            }
            return maskSensitiveFields(payload);
        } catch (UnsupportedEncodingException e) {
            return "<unsupported encoding>";
        }
    }

    private boolean isVisibleContent(String contentType) {
        if (contentType == null) {
            return true;
        }

        return contentType.startsWith(MediaType.APPLICATION_JSON_VALUE)
                || contentType.startsWith(MediaType.APPLICATION_XML_VALUE)
                || contentType.startsWith(MediaType.TEXT_PLAIN_VALUE)
                || contentType.startsWith(MediaType.TEXT_HTML_VALUE)
                || contentType.startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    }

    private String maskSensitiveFields(String payload) {
        return SENSITIVE_JSON_FIELDS.matcher(payload).replaceAll("$1<masked>$3");
    }
}
