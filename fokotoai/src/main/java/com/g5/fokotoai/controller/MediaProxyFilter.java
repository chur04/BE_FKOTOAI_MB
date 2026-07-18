package com.g5.fokotoai.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Proxy video/audio from Dung Mori CDN.
 * /api/video-proxy/** → https://vn.dungmori.com/**
 * /api/audio-proxy/** → https://mp3-v2.dungmori.com/**
 *
 * Also rewrites absolute CDN URLs inside m3u8 playlists so that
 * every HLS segment/playlist request also passes through this proxy.
 */
@Slf4j
@Component
@Order(1)
public class MediaProxyFilter implements Filter {

    private static final int MAX_REDIRECTS = 5;

    @Value("${media.cdn-video:https://vn.dungmori.com}")
    private String videoCdn;

    @Value("${media.cdn-audio:https://mp3-v2.dungmori.com}")
    private String audioCdn;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Strip context-path cleanly
        String contextPath = req.getContextPath(); // e.g. "/FKOTOAI"
        String uri = req.getRequestURI();           // e.g. "/FKOTOAI/api/video-proxy/..."
        String path = uri.startsWith(contextPath) ? uri.substring(contextPath.length()) : uri;
        // path is now "/api/video-proxy/..." or "/api/audio-proxy/..."

        String cdn = null;
        String proxyPrefix = null;

        if (path.startsWith("/api/video-proxy/")) {
            cdn = videoCdn;
            proxyPrefix = "/api/video-proxy/";
        } else if (path.startsWith("/api/audio-proxy/")) {
            cdn = audioCdn;
            proxyPrefix = "/api/audio-proxy/";
        }

        if (cdn != null) {
            String cdnPath = path.substring(proxyPrefix.length()); // e.g. "720p/file.mp4/index.m3u8"
            String qs = req.getQueryString();
            String targetUrl = cdn + "/" + cdnPath + (qs != null ? "?" + qs : "");
            proxyTo(targetUrl, req, res, contextPath);
            return;
        }

        chain.doFilter(request, response);
    }

    private void proxyTo(String targetUrl, HttpServletRequest req, HttpServletResponse res, String contextPath) {
        log.info("Proxy: {} -> {}", req.getRequestURI(), targetUrl);

        HttpURLConnection conn = null;
        try {
            conn = openConnection(targetUrl, req.getHeader("Range"));

            // Follow redirects manually (handles cross-host / HTTPS redirects)
            int redirects = 0;
            int status = conn.getResponseCode();
            while ((status == 301 || status == 302 || status == 303 || status == 307 || status == 308)
                    && redirects < MAX_REDIRECTS) {
                String location = conn.getHeaderField("Location");
                if (location == null || location.isBlank()) break;
                log.info("Proxy redirect ({}) {} -> {}", status, targetUrl, location);
                conn.disconnect();
                targetUrl = location;
                conn = openConnection(targetUrl, req.getHeader("Range"));
                status = conn.getResponseCode();
                redirects++;
            }

            res.setStatus(status);

            // Content-Type
            String ct = conn.getContentType();
            boolean isM3u8 = targetUrl.contains(".m3u8")
                    || (ct != null && ct.contains("mpegurl"))
                    || (ct != null && ct.contains("x-mpegURL"));

            if (ct != null) {
                res.setContentType(ct);
            } else {
                if (isM3u8)                                          res.setContentType("application/vnd.apple.mpegurl");
                else if (targetUrl.endsWith(".ts"))                  res.setContentType("video/mp2t");
                else if (targetUrl.endsWith(".mp3") || targetUrl.endsWith(".m4a")) res.setContentType("audio/mpeg");
            }

            String cr = conn.getHeaderField("Content-Range");
            if (cr != null) res.setHeader("Content-Range", cr);
            String ar = conn.getHeaderField("Accept-Ranges");
            if (ar != null) res.setHeader("Accept-Ranges", ar);

            res.setHeader("Access-Control-Allow-Origin",  "*");
            res.setHeader("Access-Control-Allow-Headers", "*");
            res.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");

            try (InputStream in = (status >= 200 && status < 300)
                    ? conn.getInputStream() : conn.getErrorStream();
                 OutputStream out = res.getOutputStream()) {

                if (in == null) return;

                if (isM3u8 && status >= 200 && status < 300) {
                    // Read full m3u8, rewrite CDN absolute URLs → proxy paths
                    byte[] raw = in.readAllBytes();
                    String content = new String(raw, StandardCharsets.UTF_8);
                    content = rewriteM3u8Urls(content, contextPath);
                    byte[] rewritten = content.getBytes(StandardCharsets.UTF_8);
                    res.setContentLengthLong(rewritten.length);
                    out.write(rewritten);
                } else {
                    // Stream binary (ts segments, audio, error pages, …)
                    String cl = conn.getHeaderField("Content-Length");
                    if (cl != null) res.setContentLengthLong(Long.parseLong(cl));
                    byte[] buf = new byte[16384];
                    int n;
                    while ((n = in.read(buf)) != -1) {
                        out.write(buf, 0, n);
                        out.flush();
                    }
                }
            }

        } catch (Exception e) {
            log.error("Proxy failed for {}: {}", targetUrl, e.getMessage());
            try {
                if (!res.isCommitted()) {
                    res.setStatus(502);
                    res.setContentType("application/json");
                    res.getWriter().write("{\"error\":\"Proxy failed: " + e.getMessage() + "\"}");
                }
            } catch (IOException ignored) {}
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /**
     * Rewrite absolute CDN URLs in an m3u8 playlist so every sub-request
     * also passes through the proxy.
     *
     * e.g. https://vn.dungmori.com/720p/file/seg001.ts
     *   →  /FKOTOAI/api/video-proxy/720p/file/seg001.ts
     */
    private String rewriteM3u8Urls(String content, String contextPath) {
        content = content.replace(videoCdn + "/", contextPath + "/api/video-proxy/");
        content = content.replace(audioCdn + "/", contextPath + "/api/audio-proxy/");
        return content;
    }

    private HttpURLConnection openConnection(String targetUrl, String rangeHeader) throws IOException {
        var url = URI.create(targetUrl).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(false); // we handle redirects manually
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(60_000);
        conn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/120.0.0.0 Safari/537.36");
        conn.setRequestProperty("Referer", "https://dungmori.com/");
        conn.setRequestProperty("Origin",  "https://dungmori.com");
        conn.setRequestProperty("Accept",  "*/*");
        conn.setRequestProperty("Accept-Language", "vi,en;q=0.9");
        if (rangeHeader != null) {
            conn.setRequestProperty("Range", rangeHeader);
        }
        conn.connect();
        return conn;
    }
}
