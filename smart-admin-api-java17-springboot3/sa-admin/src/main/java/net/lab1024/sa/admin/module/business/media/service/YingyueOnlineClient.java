package net.lab1024.sa.admin.module.business.media.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 影月全网检索：豆瓣 + Bangumi + iTunes 并行，中文剧名也能出结果。
 */
@Slf4j
@Component
public class YingyueOnlineClient {

    private static final Map<String, String> EN_ALIAS = Map.of(
            "斗罗大陆", "Soul Land",
            "斗破苍穹", "Battle Through the Heavens",
            "完美世界", "Perfect World donghua",
            "遮天", "Shrouding the Heavens",
            "凡人修仙传", "A Record of a Mortal's Journey to Immortality",
            "仙逆", "Renegade Immortal",
            "一人之下", "The Outcast",
            "流浪地球", "The Wandering Earth",
            "哪吒", "Nezha",
            "三体", "The Three-Body Problem"
    );

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Resource
    private ObjectMapper objectMapper;

    private volatile List<Map<String, Object>> trendingCache = List.of();
    private volatile long trendingAt;

    public List<Map<String, Object>> search(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return List.of();
        }
        String q = keyword.trim();
        String en = EN_ALIAS.entrySet().stream()
                .filter(e -> q.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(q);
        try {
            CompletableFuture<List<Map<String, Object>>> douban = CompletableFuture.supplyAsync(() -> douban(q));
            CompletableFuture<List<Map<String, Object>>> bangumi = CompletableFuture.supplyAsync(() -> bangumi(q));
            CompletableFuture<List<Map<String, Object>>> movies = CompletableFuture.supplyAsync(() -> itunes(q, "movie", 1, "CN"));
            CompletableFuture<List<Map<String, Object>>> tvs = CompletableFuture.supplyAsync(() -> itunes(q, "tvSeason", 2, "CN"));
            CompletableFuture<List<Map<String, Object>>> enMovies = CompletableFuture.supplyAsync(() -> itunes(en, "movie", 1, "US"));
            CompletableFuture.allOf(douban, bangumi, movies, tvs, enMovies).get(7, TimeUnit.SECONDS);
            LinkedHashMap<String, Map<String, Object>> merged = new LinkedHashMap<>();
            addAll(merged, douban.get());
            addAll(merged, bangumi.get());
            addAll(merged, movies.get());
            addAll(merged, tvs.get());
            addAll(merged, enMovies.get());
            fillPlayUrl(merged);
            return new ArrayList<>(merged.values());
        } catch (Exception e) {
            log.warn("影月联网搜索失败: {}", e.getMessage());
            List<Map<String, Object>> fallback = douban(q);
            fallback.addAll(bangumi(q));
            fillPlayUrl(toMap(fallback));
            return fallback;
        }
    }

    public List<Map<String, Object>> trending() {
        if (!trendingCache.isEmpty() && System.currentTimeMillis() - trendingAt < 15 * 60 * 1000) {
            return trendingCache;
        }
        LinkedHashMap<String, Map<String, Object>> merged = new LinkedHashMap<>();
        try {
            CompletableFuture<List<Map<String, Object>>> a = CompletableFuture.supplyAsync(() -> itunes("电影", "movie", 1, "CN"));
            CompletableFuture<List<Map<String, Object>>> b = CompletableFuture.supplyAsync(() -> douban("热门"));
            CompletableFuture.allOf(a, b).get(6, TimeUnit.SECONDS);
            addAll(merged, a.get());
            addAll(merged, b.get());
            fillPlayUrl(merged);
        } catch (Exception e) {
            log.warn("影月热播拉取失败: {}", e.getMessage());
        }
        trendingCache = new ArrayList<>(merged.values());
        trendingAt = System.currentTimeMillis();
        return trendingCache;
    }

    public String resolvePlayUrl(String playUrl) {
        if (StringUtils.isNotBlank(playUrl)) {
            return playUrl;
        }
        return MediaSupport.VIDEO[0];
    }

    private List<Map<String, Object>> douban(String keyword) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            JsonNode root = getJson("https://movie.douban.com/j/subject_suggest?q=" + enc(keyword), "https://movie.douban.com/");
            if (!root.isArray()) {
                return list;
            }
            for (JsonNode node : root) {
                String title = firstText(node, "title");
                if (StringUtils.isBlank(title)) {
                    continue;
                }
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("title", title);
                row.put("category", "movie".equals(node.path("type").asText()) ? 1 : 2);
                row.put("area", "cn");
                row.put("year", yearOf(firstText(node, "year")));
                row.put("score", new BigDecimal("8.4"));
                row.put("episodeCount", 1);
                row.put("updateInfo", "豆瓣全网条目");
                row.put("playUrl", "");
                row.put("coverUrl", firstText(node, "img").replace("/s_ratio_poster/", "/m_ratio_poster/"));
                row.put("intro", StringUtils.defaultIfBlank(firstText(node, "sub_title"), title));
                row.put("source", "douban");
                row.put("sourceId", firstText(node, "id"));
                list.add(row);
            }
        } catch (Exception e) {
            log.warn("豆瓣搜索失败: {}", e.getMessage());
        }
        return list;
    }

    private List<Map<String, Object>> bangumi(String keyword) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            JsonNode root = getJson("https://api.bgm.tv/search/subject/" + enc(keyword) + "?type=2&responseGroup=small", "https://bgm.tv/");
            JsonNode listNode = root.path("list");
            if (!listNode.isArray()) {
                return list;
            }
            for (JsonNode node : listNode) {
                String title = StringUtils.defaultIfBlank(firstText(node, "name_cn"), firstText(node, "name"));
                if (StringUtils.isBlank(title)) {
                    continue;
                }
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("title", title);
                row.put("category", 4);
                row.put("area", "cn");
                row.put("year", yearOf(node.path("air_date").asText(node.path("date").asText(""))));
                row.put("score", scoreOf(node.path("rating").path("score").asDouble(8.0) / 2));
                row.put("episodeCount", Math.max(1, node.path("eps").asInt(1)));
                row.put("updateInfo", "Bangumi 动画");
                row.put("playUrl", "");
                row.put("coverUrl", firstText(node.path("images"), "large", "common", "medium"));
                row.put("intro", StringUtils.defaultIfBlank(firstText(node, "name"), title));
                row.put("source", "bangumi");
                row.put("sourceId", node.path("id").asText());
                list.add(row);
            }
        } catch (Exception e) {
            log.warn("Bangumi 搜索失败: {}", e.getMessage());
        }
        return list;
    }

    private List<Map<String, Object>> itunes(String term, String entity, int category, String country) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            String url = "https://itunes.apple.com/search?term=" + enc(term)
                    + "&entity=" + entity + "&limit=20&country=" + country + "&lang=zh_cn";
            JsonNode results = getJson(url, "https://itunes.apple.com/").path("results");
            if (!results.isArray()) {
                return list;
            }
            for (JsonNode node : results) {
                String title = firstText(node, "trackName", "collectionName", "trackCensoredName");
                if (StringUtils.isBlank(title)) {
                    continue;
                }
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("title", title);
                row.put("category", category);
                row.put("area", areaOf(firstText(node, "country")));
                row.put("year", yearOf(firstText(node, "releaseDate")));
                row.put("score", scoreOf(node.path("averageUserRating").asDouble(0)));
                row.put("episodeCount", Math.max(1, node.path("trackCount").asInt(1)));
                String preview = firstText(node, "previewUrl");
                row.put("updateInfo", StringUtils.isNotBlank(preview) ? "官方预告" : "iTunes 条目");
                row.put("playUrl", preview);
                String cover = firstText(node, "artworkUrl100").replace("100x100bb", "400x400bb");
                row.put("coverUrl", StringUtils.defaultIfBlank(cover, MediaSupport.cover("yy" + title.hashCode(), 360, 500)));
                row.put("intro", StringUtils.defaultIfBlank(firstText(node, "longDescription", "shortDescription"), title));
                row.put("source", "itunes");
                row.put("sourceId", firstText(node, "trackId", "collectionId"));
                list.add(row);
            }
        } catch (Exception e) {
            log.warn("iTunes {} 检索失败: {}", entity, e.getMessage());
        }
        return list;
    }

    private void fillPlayUrl(Map<String, Map<String, Object>> merged) {
        int i = 0;
        for (Map<String, Object> item : merged.values()) {
            if (StringUtils.isNotBlank(String.valueOf(item.getOrDefault("playUrl", "")))) {
                continue;
            }
            item.put("playUrl", MediaSupport.VIDEO[i % MediaSupport.VIDEO.length]);
            item.put("updateInfo", StringUtils.defaultIfBlank(String.valueOf(item.get("updateInfo")), "全网条目") + " · 正片受版权限制");
            i++;
        }
    }

    private void addAll(Map<String, Map<String, Object>> merged, List<Map<String, Object>> list) {
        for (Map<String, Object> item : list) {
            merged.putIfAbsent(keyOf(item), item);
        }
    }

    private LinkedHashMap<String, Map<String, Object>> toMap(List<Map<String, Object>> list) {
        LinkedHashMap<String, Map<String, Object>> map = new LinkedHashMap<>();
        addAll(map, list);
        return map;
    }

    private JsonNode getJson(String url, String referer) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/122.0.0.0 Safari/537.36")
                .header("Referer", referer)
                .header("Accept", "application/json,text/plain,*/*")
                .GET()
                .build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() >= 400) {
            throw new IllegalStateException("HTTP " + response.statusCode());
        }
        return objectMapper.readTree(response.body());
    }

    private String enc(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    private String firstText(JsonNode node, String... fields) {
        for (String field : fields) {
            String value = node.path(field).asText("");
            if (StringUtils.isNotBlank(value) && !"null".equals(value)) {
                return value;
            }
        }
        return "";
    }

    private String areaOf(String country) {
        if ("CHN".equalsIgnoreCase(country) || "CN".equalsIgnoreCase(country)) {
            return "cn";
        }
        if ("JPN".equalsIgnoreCase(country) || "JP".equalsIgnoreCase(country)) {
            return "jp";
        }
        if ("KOR".equalsIgnoreCase(country) || "KR".equalsIgnoreCase(country)) {
            return "kr";
        }
        return "us";
    }

    private Integer yearOf(String releaseDate) {
        if (StringUtils.length(releaseDate) >= 4) {
            try {
                return Integer.parseInt(releaseDate.substring(0, 4));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private BigDecimal scoreOf(double rating) {
        if (rating <= 0) {
            return new BigDecimal("8.0");
        }
        return BigDecimal.valueOf(Math.min(9.9, rating <= 5 ? rating * 2 : rating)).setScale(1, java.math.RoundingMode.HALF_UP);
    }

    private String keyOf(Map<String, Object> item) {
        return String.valueOf(item.get("source")) + "#" + String.valueOf(item.get("sourceId"));
    }
}
