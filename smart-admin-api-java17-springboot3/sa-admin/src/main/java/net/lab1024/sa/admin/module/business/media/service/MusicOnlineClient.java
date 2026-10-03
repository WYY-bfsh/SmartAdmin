package net.lab1024.sa.admin.module.business.media.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

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

/**
 * 全网搜歌：优先网易云，失败则走 iTunes 公开接口。
 */
@Slf4j
@Component
public class MusicOnlineClient {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Resource
    private ObjectMapper objectMapper;

    public List<Map<String, Object>> search(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return List.of();
        }
        List<Map<String, Object>> netease = searchNetease(keyword);
        if (!netease.isEmpty()) {
            return netease;
        }
        return searchItunes(keyword);
    }

    public String resolvePlayUrl(String audioUrl, String name, String artist) {
        Long neteaseId = parseNeteaseId(audioUrl);
        if (neteaseId != null) {
            String real = neteasePlayUrl(neteaseId);
            if (StringUtils.isNotBlank(real)) {
                return real;
            }
        }
        if (StringUtils.isNotBlank(audioUrl) && !audioUrl.contains("music.163.com")) {
            return audioUrl;
        }
        if (StringUtils.isNotBlank(name)) {
            List<Map<String, Object>> itunes = searchItunes(StringUtils.trimToEmpty(name) + " " + StringUtils.trimToEmpty(artist));
            if (!itunes.isEmpty()) {
                return String.valueOf(itunes.get(0).get("audioUrl"));
            }
        }
        return audioUrl;
    }

    private String neteasePlayUrl(Long id) {
        try {
            JsonNode data = getJson("https://music.163.com/api/song/enhance/player/url?id=" + id + "&ids=[" + id + "]&br=128000").path("data");
            if (data.isArray() && !data.isEmpty()) {
                String url = data.get(0).path("url").asText("");
                if (StringUtils.isNotBlank(url) && !"null".equals(url)) {
                    return url;
                }
            }
        } catch (Exception e) {
            log.warn("解析网易云播放地址失败: {}", e.getMessage());
        }
        return "https://music.163.com/song/media/outer/url?id=" + id + ".mp3";
    }

    public List<Map<String, Object>> lyric(String audioUrl) {
        Long neteaseId = parseNeteaseId(audioUrl);
        if (neteaseId == null) {
            return List.of();
        }
        try {
            JsonNode root = getJson("https://music.163.com/api/song/lyric?id=" + neteaseId + "&lv=-1&kv=-1&tv=-1");
            String lrc = root.path("lrc").path("lyric").asText("");
            return parseLrc(lrc);
        } catch (Exception e) {
            log.warn("拉取歌词失败: {}", e.getMessage());
            return List.of();
        }
    }

    private List<Map<String, Object>> searchNetease(String keyword) {
        try {
            String url = "https://music.163.com/api/search/get/web?s="
                    + URLEncoder.encode(keyword, StandardCharsets.UTF_8)
                    + "&type=1&offset=0&limit=20";
            JsonNode songs = getJson(url).path("result").path("songs");
            if (!songs.isArray() || songs.isEmpty()) {
                return List.of();
            }
            List<String> ids = new ArrayList<>();
            for (JsonNode song : songs) {
                ids.add(song.path("id").asText());
            }
            Map<String, JsonNode> detailMap = songDetails(ids);
            List<Map<String, Object>> list = new ArrayList<>();
            for (JsonNode song : songs) {
                String id = song.path("id").asText();
                JsonNode detail = detailMap.getOrDefault(id, song);
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("name", song.path("name").asText());
                row.put("artist", firstArtist(song));
                row.put("album", song.path("album").path("name").asText(""));
                row.put("duration", formatDuration(song.path("duration").asLong(0)));
                row.put("audioUrl", "https://music.163.com/song/media/outer/url?id=" + id + ".mp3");
                String cover = detail.path("album").path("picUrl").asText("");
                if (StringUtils.isBlank(cover)) {
                    cover = MediaSupport.cover("ne" + id, 240, 240);
                }
                row.put("coverUrl", cover);
                list.add(row);
            }
            return list;
        } catch (Exception e) {
            log.warn("网易云搜索失败，将尝试 iTunes: {}", e.getMessage());
            return List.of();
        }
    }

    private Map<String, JsonNode> songDetails(List<String> ids) {
        Map<String, JsonNode> map = new LinkedHashMap<>();
        if (ids.isEmpty()) {
            return map;
        }
        try {
            JsonNode songs = getJson("https://music.163.com/api/song/detail?ids=[" + String.join(",", ids) + "]").path("songs");
            if (songs.isArray()) {
                for (JsonNode song : songs) {
                    map.put(song.path("id").asText(), song);
                }
            }
        } catch (Exception e) {
            log.warn("网易云歌曲详情失败: {}", e.getMessage());
        }
        return map;
    }

    private List<Map<String, Object>> searchItunes(String keyword) {
        try {
            String url = "https://itunes.apple.com/search?term="
                    + URLEncoder.encode(keyword, StandardCharsets.UTF_8)
                    + "&entity=song&limit=20&country=CN";
            JsonNode results = getJson(url).path("results");
            if (!results.isArray()) {
                return List.of();
            }
            List<Map<String, Object>> list = new ArrayList<>();
            for (JsonNode song : results) {
                String preview = song.path("previewUrl").asText("");
                if (StringUtils.isBlank(preview)) {
                    continue;
                }
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("name", song.path("trackName").asText());
                row.put("artist", song.path("artistName").asText());
                row.put("album", song.path("collectionName").asText(""));
                row.put("duration", formatDuration(song.path("trackTimeMillis").asLong(0)));
                row.put("audioUrl", preview);
                String cover = song.path("artworkUrl100").asText("").replace("100x100bb", "240x240bb");
                row.put("coverUrl", StringUtils.defaultIfBlank(cover, MediaSupport.cover("it" + song.path("trackId").asText(), 240, 240)));
                list.add(row);
            }
            return list;
        } catch (Exception e) {
            log.warn("iTunes 搜索失败: {}", e.getMessage());
            return List.of();
        }
    }

    private JsonNode getJson(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Referer", "https://music.163.com/")
                .GET()
                .build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() >= 400) {
            throw new IllegalStateException("HTTP " + response.statusCode());
        }
        return objectMapper.readTree(response.body());
    }

    private String firstArtist(JsonNode song) {
        JsonNode artists = song.path("artists");
        if (artists.isArray() && !artists.isEmpty()) {
            return artists.get(0).path("name").asText("");
        }
        return song.path("artist").asText("");
    }

    private String formatDuration(long millis) {
        if (millis <= 0) {
            return "00:00";
        }
        long sec = millis / 1000;
        return String.format("%02d:%02d", sec / 60, sec % 60);
    }

    private Long parseNeteaseId(String audioUrl) {
        if (StringUtils.isBlank(audioUrl) || !audioUrl.contains("music.163.com")) {
            return null;
        }
        int idx = audioUrl.indexOf("id=");
        if (idx < 0) {
            return null;
        }
        String raw = audioUrl.substring(idx + 3).replace(".mp3", "");
        int end = raw.indexOf('&');
        if (end > 0) {
            raw = raw.substring(0, end);
        }
        return MediaSupport.toLong(raw);
    }

    private List<Map<String, Object>> parseLrc(String lrc) {
        List<Map<String, Object>> lines = new ArrayList<>();
        if (StringUtils.isBlank(lrc)) {
            return lines;
        }
        for (String row : lrc.split("\\r?\\n")) {
            int close = row.indexOf(']');
            if (close < 3 || !row.startsWith("[")) {
                continue;
            }
            String timeText = row.substring(1, close);
            String text = row.substring(close + 1).trim();
            if (StringUtils.isBlank(text)) {
                continue;
            }
            String[] parts = timeText.split(":");
            if (parts.length < 2) {
                continue;
            }
            try {
                double minutes = Double.parseDouble(parts[0]);
                double seconds = Double.parseDouble(parts[1]);
                Map<String, Object> line = new LinkedHashMap<>();
                line.put("time", (int) (minutes * 60 + seconds));
                line.put("text", text);
                lines.add(line);
            } catch (NumberFormatException ignored) {
                // skip invalid lrc row
            }
        }
        return lines;
    }
}
