package com.sahayak.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Filter that patches the gap between AWS Lambda's servlet adapter and Spring 6.
 *
 * <p>Problem: Spring 6's {@code StandardMultipartHttpServletRequest} no longer
 * overrides {@code getParameter()} / {@code getParameterValues()}; it relies on
 * the servlet container (Tomcat, Jetty) to make multipart form-field values
 * available through those methods.  AWS's {@code AwsProxyHttpServletRequest}
 * only returns query-string and url-encoded-form params from those methods,
 * so multipart text fields are invisible to {@code @RequestParam}.
 *
 * <p>Fix: this filter wraps every multipart request so that
 * {@code getParameter()} and {@code getParameterValues()} also consult
 * {@code getPart()} for plain-text form fields.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LambdaMultipartFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String ct = request.getContentType();
        if (ct != null && ct.toLowerCase(Locale.ROOT).startsWith("multipart/")) {
            filterChain.doFilter(new MultipartParamWrapper(request), response);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Wrapper that adds multipart form-field values to the parameter API.
     * File-upload parts (those with a submitted file name) are left alone.
     */
    private static class MultipartParamWrapper extends HttpServletRequestWrapper {

        /** Lazily populated cache: form-field name → values */
        private volatile Map<String, String[]> multipartParams;

        MultipartParamWrapper(HttpServletRequest request) {
            super(request);
        }

        // ----------------------------------------------------------
        // Lazily parse multipart text fields into a parameter map
        // ----------------------------------------------------------
        private Map<String, String[]> getMultipartParams() {
            if (multipartParams != null) {
                return multipartParams;
            }
            synchronized (this) {
                if (multipartParams != null) return multipartParams;
                Map<String, List<String>> tmp = new LinkedHashMap<>();
                try {
                    HttpServletRequest httpRequest = (HttpServletRequest) getRequest();
                    for (Part part : httpRequest.getParts()) {
                        // Skip file-upload parts
                        if (part.getSubmittedFileName() != null) continue;
                        String value = new String(
                                part.getInputStream().readAllBytes(),
                                StandardCharsets.UTF_8);
                        tmp.computeIfAbsent(part.getName(), k -> new ArrayList<>())
                                .add(value);
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse multipart params: {}", e.getMessage());
                }
                Map<String, String[]> result = new LinkedHashMap<>();
                tmp.forEach((k, v) -> result.put(k, v.toArray(new String[0])));
                multipartParams = result;
                return result;
            }
        }

        // ----------------------------------------------------------
        // Override parameter accessors
        // ----------------------------------------------------------

        @Override
        public String getParameter(String name) {
            // Try the original request first (query params, url-encoded)
            String val = super.getParameter(name);
            if (val != null) return val;
            // Fall back to multipart text fields
            String[] mp = getMultipartParams().get(name);
            return mp != null && mp.length > 0 ? mp[0] : null;
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] original = super.getParameterValues(name);
            String[] mp = getMultipartParams().get(name);
            if (mp == null) return original;
            if (original == null) return mp;
            // merge
            String[] merged = new String[original.length + mp.length];
            System.arraycopy(original, 0, merged, 0, original.length);
            System.arraycopy(mp, 0, merged, original.length, mp.length);
            return merged;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            Map<String, String[]> base = new LinkedHashMap<>(super.getParameterMap());
            getMultipartParams().forEach((name, values) -> {
                if (!base.containsKey(name)) {
                    base.put(name, values);
                }
            });
            return Collections.unmodifiableMap(base);
        }

        @Override
        public Enumeration<String> getParameterNames() {
            Set<String> names = new LinkedHashSet<>();
            Enumeration<String> orig = super.getParameterNames();
            while (orig.hasMoreElements()) {
                names.add(orig.nextElement());
            }
            names.addAll(getMultipartParams().keySet());
            return Collections.enumeration(names);
        }
    }
}
