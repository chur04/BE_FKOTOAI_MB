package com.g5.fokotoai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.g5.fokotoai.exception.AppException;
import com.g5.fokotoai.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CourseService {

    RestClient restClient;
    ObjectMapper objectMapper;

    @NonFinal
    @Value("${course.courses-json-url}")
    String coursesJsonUrl;

    @NonFinal
    @Value("${course.data-json-url-template}")
    String dataJsonUrlTemplate;

    @NonFinal
    @Value("${course.local-data-dir:}")
    String localDataDir;

    /**
     * Map broken course slugs to working ones for fallback.
     * Format: key1:val1,key2:val2 (e.g., khoa-n1:jlpt-n1,khoa-n2:jlpt-n2)
     */
    @NonFinal
    @Value("${course.slug-aliases:}")
    String slugAliasesRaw;

    /** Cache course metadata (slug -> course info) for enriching fallback responses */
    private final Map<String, Map<String, Object>> courseMetaCache = new ConcurrentHashMap<>();

    /**
     * Get list of all Dung Mori courses.
     * Also populates the course metadata cache for fallback enrichment.
     */
    public List<Map<String, Object>> getCourses() {
        try {
            String raw = restClient.get()
                    .uri(coursesJsonUrl + "?t=" + System.currentTimeMillis())
                    .retrieve()
                    .body(String.class);

            log.debug("Courses JSON size: {} chars", raw != null ? raw.length() : 0);

            List<Map<String, Object>> courses = objectMapper.readValue(
                    raw, new TypeReference<List<Map<String, Object>>>() {});

            // Populate metadata cache
            for (Map<String, Object> course : courses) {
                Object seoUrl = course.get("SEOurl");
                if (seoUrl != null) {
                    courseMetaCache.put(seoUrl.toString(), course);
                }
            }

            return courses;

        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                log.error("Course list API timeout", e);
                throw new AppException(ErrorCode.COURSE_API_TIMEOUT);
            }
            log.error("Course list connection error", e);
            throw new AppException(ErrorCode.COURSE_API_ERROR);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Course list unexpected error", e);
            throw new AppException(ErrorCode.COURSE_API_ERROR);
        }
    }

    /**
     * Get full course content by slug (e.g., "so-cap-n5", "jlpt-n1").
     * Falls back to scraping dungmori.com when CDN data is unavailable,
     * and finally to an empty course structure.
     */
    public Map<String, Object> getCourseData(String slug) {
        try {
            String url = dataJsonUrlTemplate.replace("{slug}", slug);
            String raw = restClient.get()
                    .uri(url + "?t=" + System.currentTimeMillis())
                    .retrieve()
                    .body(String.class);

            log.info("Course data loaded for slug={}, size={} chars", slug, raw != null ? raw.length() : 0);

            // Detect HTML fallback from CDN — data file is missing
            if (raw != null && raw.stripLeading().startsWith("<")) {
                log.warn("Course data not found on CDN for slug={}, trying fallbacks", slug);

                // Try local file first
                Map<String, Object> localData = tryLoadLocalFile(slug);
                if (localData != null) {
                    normalizeCategories(localData);
                    return localData;
                }

                // Try alias mapping (e.g., khoa-n1 -> jlpt-n1)
                Map<String, Object> aliasData = tryLoadAlias(slug);
                if (aliasData != null) {
                    normalizeCategories(aliasData);
                    return aliasData;
                }

                // Try scraping dungmori.com for courses with embedded ldknGroups data
                Map<String, Object> scraped = tryScrapeDungMori(slug);
                if (scraped != null) {
                    return scraped;
                }

                // Last resort: enriched empty structure
                log.warn("No data available for slug={}, serving empty structure", slug);
                return buildEmptyCourseData(slug);
            }

            Map<String, Object> result = objectMapper.readValue(raw, new TypeReference<Map<String, Object>>() {});
            normalizeCategories(result);
            return result;

        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                log.error("Course data API timeout for slug={}", slug, e);
                throw new AppException(ErrorCode.COURSE_API_TIMEOUT);
            }
            log.error("Course data connection error for slug={}", slug, e);
            throw new AppException(ErrorCode.COURSE_API_ERROR);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Course data unexpected error for slug={}", slug, e);
            throw new AppException(ErrorCode.COURSE_API_ERROR);
        }
    }

    /**
     * Normalize course data: flatten subgroups into groups so the frontend
     * can render them (frontend expects categories → groups → lessons).
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> normalizeCategories(Map<String, Object> course) {
        List<Map<String, Object>> categories = (List<Map<String, Object>>) course.get("categories");
        if (categories == null) {
            return course;
        }
        for (Map<String, Object> category : categories) {
            List<Map<String, Object>> groups = (List<Map<String, Object>>) category.get("groups");
            if (groups == null) continue;

            List<Map<String, Object>> flattened = new ArrayList<>();
            for (Map<String, Object> group : groups) {
                List<Map<String, Object>> subgroups = (List<Map<String, Object>>) group.get("subgroups");
                if (subgroups != null && !subgroups.isEmpty()) {
                    // Flatten: each subgroup becomes a top-level group
                    for (Map<String, Object> sub : subgroups) {
                        // Copy group-level fields, then override with subgroup fields
                        Map<String, Object> flatGroup = new LinkedHashMap<>(group);
                        flatGroup.remove("subgroups");
                        flatGroup.put("id", sub.getOrDefault("id", group.get("id")));
                        flatGroup.put("name", sub.getOrDefault("name", group.get("name")));
                        flatGroup.put("lessons", sub.get("lessons"));
                        flattened.add(flatGroup);
                    }
                } else {
                    flattened.add(group);
                }
            }
            category.put("groups", flattened);
        }
        return course;
    }

    /**
     * Build a minimal course structure enriched with metadata from the course list.
     */
    private Map<String, Object> buildEmptyCourseData(String slug) {
        Map<String, Object> course = new LinkedHashMap<>();
        course.put("slug", slug);

        // Look up course name from cached metadata
        Map<String, Object> meta = courseMetaCache.get(slug);
        String name = (meta != null) ? (String) meta.get("name") : slug;
        course.put("name", name != null ? name : slug);

        Map<String, String> cdn = new HashMap<>();
        cdn.put("vn", "https://vn.dungmori.com");
        cdn.put("jp", "https://tokyo-v2.dungmori.com");
        course.put("cdn", cdn);

        course.put("hls_key", "");
        course.put("categories", Collections.emptyList());

        return course;
    }

    // Pattern to extract JS array: var ldknGroups = [...];
    private static final Pattern LDKN_GROUPS_PATTERN =
            Pattern.compile("var\\s+ldknGroups\\s*=\\s*(\\[.*?\\]);", Pattern.DOTALL);

    // Pattern to extract JS array: var ldLessons = [...];
    private static final Pattern LD_LESSONS_PATTERN =
            Pattern.compile("var\\s+ldLessons\\s*=\\s*(\\[.*?\\]);", Pattern.DOTALL);

    /**
     * Try to load course data from a local directory.
     * Files should be named {slug}.json (e.g., luyen-de-n4.json).
     * Returns null if local data dir is not configured or file doesn't exist.
     */
    private Map<String, Object> tryLoadLocalFile(String slug) {
        if (localDataDir == null || localDataDir.isBlank()) {
            return null;
        }
        try {
            Path filePath = Paths.get(localDataDir, slug + ".json");
            if (!Files.exists(filePath)) {
                log.debug("No local file for slug={} at {}", slug, filePath);
                return null;
            }
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            if (content.stripLeading().startsWith("<")) {
                log.warn("Local file for slug={} appears to be HTML, skipping", slug);
                return null;
            }
            Map<String, Object> data = objectMapper.readValue(
                    content, new TypeReference<Map<String, Object>>() {});
            log.info("Loaded course data from local file for slug={}: {} chars", slug, content.length());
            return data;
        } catch (Exception e) {
            log.warn("Failed to load local file for slug={}: {}", slug, e.getMessage());
            return null;
        }
    }

    /**
     * Try to load course data via slug alias mapping.
     * E.g., khoa-n1 -> jlpt-n1, using the aliased course's data
     * but with slug/name overridden to match the requested slug.
     */
    private Map<String, Object> tryLoadAlias(String slug) {
        String target = resolveAlias(slug);
        if (target == null) {
            return null;
        }
        log.info("Resolving alias: {} -> {}", slug, target);

        // Try loading the target course data (local file first, then CDN)
        Map<String, Object> data = tryLoadLocalFile(target);
        if (data == null) {
            data = tryLoadFromCdnDirect(target);
        }
        if (data == null) {
            log.warn("Alias target {} not available for slug={}", target, slug);
            return null;
        }

        // Override slug and name to match the requested course
        Map<String, Object> result = new LinkedHashMap<>(data);
        result.put("slug", slug);
        Map<String, Object> meta = courseMetaCache.get(slug);
        String name = (meta != null) ? (String) meta.get("name") : slug;
        result.put("name", name != null ? name : slug);

        log.info("Serving aliased course data for slug={} from target={}", slug, target);
        return result;
    }

    /** Resolve a slug alias from config, returns target slug or null. */
    private String resolveAlias(String slug) {
        if (slugAliasesRaw == null || slugAliasesRaw.isBlank()) {
            return null;
        }
        for (String pair : slugAliasesRaw.split(",")) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2 && kv[0].trim().equals(slug)) {
                return kv[1].trim();
            }
        }
        return null;
    }

    /**
     * Try loading course data directly from CDN (no fallback loop).
     */
    private Map<String, Object> tryLoadFromCdnDirect(String slug) {
        try {
            String url = dataJsonUrlTemplate.replace("{slug}", slug);
            String raw = restClient.get()
                    .uri(url + "?t=" + System.currentTimeMillis())
                    .retrieve()
                    .body(String.class);

            if (raw != null && !raw.stripLeading().startsWith("<")) {
                return objectMapper.readValue(raw, new TypeReference<Map<String, Object>>() {});
            }
        } catch (Exception e) {
            log.debug("CDN direct load failed for slug={}: {}", slug, e.getMessage());
        }
        return null;
    }

    /**
     * Try to scrape course data from dungmori.com course pages.
     * Only works for courses that embed ldknGroups data (luyện đề N1-N3 style).
     * Returns null if scraping fails.
     */
    private Map<String, Object> tryScrapeDungMori(String slug) {
        try {
            String pageUrl = "https://dungmori.com/khoa-hoc/" + slug;
            log.info("Attempting to scrape dungmori.com for slug={}", slug);

            String html = restClient.get()
                    .uri(pageUrl)
                    .retrieve()
                    .body(String.class);

            if (html == null || html.length() < 1000) {
                log.info("Page too small or missing for slug={}", slug);
                return null;
            }

            // Extract ldknGroups (groups with embedded lessons)
            Matcher groupsMatcher = LDKN_GROUPS_PATTERN.matcher(html);
            if (!groupsMatcher.find()) {
                log.info("No ldknGroups data found in page for slug={}", slug);
                return null;
            }

            String groupsJson = groupsMatcher.group(1);
            List<Map<String, Object>> groups = objectMapper.readValue(
                    groupsJson, new TypeReference<List<Map<String, Object>>>() {});

            log.info("Scraped {} groups from dungmori.com for slug={}", groups.size(), slug);

            // Build course response mimicking the standard data.json format
            Map<String, Object> course = new LinkedHashMap<>();
            course.put("slug", slug);
            course.put("name", slug);

            Map<String, String> cdn = new HashMap<>();
            cdn.put("vn", "https://vn.dungmori.com");
            cdn.put("jp", "https://tokyo-v2.dungmori.com");
            course.put("cdn", cdn);
            course.put("hls_key", "hzp8ZzlktRHGrkbkKDqVqQfvh5y4PD0D");

            // Convert ldknGroups format to categories format
            // Each group becomes a category with a single group inside
            List<Map<String, Object>> categories = new ArrayList<>();
            for (Map<String, Object> group : groups) {
                Map<String, Object> category = new LinkedHashMap<>();
                category.put("title", group.getOrDefault("name", ""));
                category.put("id", group.getOrDefault("id", 0));

                Map<String, Object> groupWrapper = new LinkedHashMap<>();
                groupWrapper.put("id", group.getOrDefault("id", 0));
                groupWrapper.put("name", group.getOrDefault("name", ""));

                // Convert lessons from ldknGroups format to standard format
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> rawLessons = (List<Map<String, Object>>) group.get("lessons");
                List<Map<String, Object>> lessons = new ArrayList<>();

                if (rawLessons != null) {
                    for (Map<String, Object> rawLesson : rawLessons) {
                        Map<String, Object> lesson = new LinkedHashMap<>();
                        lesson.put("id", rawLesson.getOrDefault("id", 0));
                        lesson.put("name", rawLesson.getOrDefault("name", ""));
                        lesson.put("slug", rawLesson.getOrDefault("SEOurl", ""));
                        lesson.put("type", rawLesson.getOrDefault("type", ""));
                        lesson.put("lesson_type", rawLesson.getOrDefault("type", ""));
                        lesson.put("is_free", false);
                        lesson.put("expect_time", rawLesson.getOrDefault("expect_time", 0));
                        lesson.put("web_url", "https://dungmori.com/khoa-hoc/" + slug + "/"
                                + rawLesson.get("id") + "-" + rawLesson.get("SEOurl"));

                        // Extract videos if present
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> videos = (List<Map<String, Object>>) rawLesson.get("videos");
                        if (videos != null && !videos.isEmpty()) {
                            Map<String, Object> firstVideo = videos.get(0);
                            lesson.put("video_name", firstVideo.getOrDefault("video_name", ""));
                            lesson.put("video_url", null);
                        } else {
                            lesson.put("video_name", null);
                            lesson.put("video_url", null);
                        }

                        lesson.put("test_data", null);
                        lesson.put("document_url", null);
                        lesson.put("document_name", null);
                        lesson.put("flashcard_data", null);
                        lesson.put("stream_url", null);
                        lesson.put("image", null);

                        lessons.add(lesson);
                    }
                }

                groupWrapper.put("lessons", lessons);
                category.put("groups", Collections.singletonList(groupWrapper));
                categories.add(category);
            }

            course.put("categories", categories);
            log.info("Scraped course data for slug={}: {} categories", slug, categories.size());
            normalizeCategories(course);
            return course;

        } catch (Exception e) {
            log.warn("Failed to scrape dungmori.com for slug={}: {}", slug, e.getMessage());
            return null;
        }
    }
}
