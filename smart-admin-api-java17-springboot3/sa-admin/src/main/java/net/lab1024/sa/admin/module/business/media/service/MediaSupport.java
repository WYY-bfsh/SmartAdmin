package net.lab1024.sa.admin.module.business.media.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MediaSupport {

    public static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final String[] AUDIO = {
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-16.mp3"
    };

    public static final String[] VIDEO = {
            "https://sf1-cdn-tos.huoshanstatic.com/obj/media-fe/xgplayer_doc_video/mp4/xgplayer-demo-720p.mp4",
            "https://www.runoob.com/try/demo_source/movie.mp4",
            "https://media.w3.org/2010/05/sintel/trailer.mp4",
            "https://sf1-cdn-tos.huoshanstatic.com/obj/media-fe/xgplayer_doc_video/mp4/xgplayer-demo-720p.mp4",
            "https://www.runoob.com/try/demo_source/movie.mp4"
    };

    public static final String KIND_TEMPLATE = "ai_template";
    public static final String KIND_SCRIPT = "ai_script";
    public static final String KIND_ARTIST = "music_artist";
    public static final String KIND_ALBUM = "music_album";
    public static final String KIND_RADIO = "music_radio";
    public static final String KIND_MUSIC_BANNER = "music_banner";
    public static final String KIND_YINGYUE_BANNER = "yingyue_banner";

    public static final String ACTION_LIKE_SONG = "like_song";
    public static final String ACTION_RECENT_SONG = "recent_song";
    public static final String ACTION_FAVORITE_VIDEO = "favorite_video";

    private MediaSupport() {
    }

    public static String cover(String seed, int w, int h) {
        return "https://picsum.photos/seed/" + seed + "/" + w + "/" + h;
    }

    public static String cover(String seed) {
        return cover(seed, 320, 180);
    }

    public static String now() {
        return LocalDateTime.now().format(TIME);
    }

    public static String formatTime(LocalDateTime time) {
        return time == null ? now() : time.format(TIME);
    }

    public static Long toLong(Object value) {
        if (value == null || "".equals(value)) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Integer toInt(Object value) {
        if (value == null || "".equals(value)) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + "B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.1fKB", bytes / 1024.0);
        }
        return String.format("%.1fMB", bytes / 1024.0 / 1024);
    }

    public static Integer guessMaterialType(String filename) {
        String name = filename == null ? "" : filename.toLowerCase();
        if (name.endsWith(".mp4") || name.endsWith(".mov") || name.endsWith(".mkv") || name.endsWith(".webm")) {
            return 1;
        }
        if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif") || name.endsWith(".webp")) {
            return 2;
        }
        if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".aac") || name.endsWith(".flac") || name.endsWith(".m4a")) {
            return 3;
        }
        return 4;
    }

    public static Map<String, Object> readMap(ObjectMapper mapper, String json) {
        if (StringUtils.isBlank(json)) {
            return new LinkedHashMap<>();
        }
        try {
            return mapper.readValue(json, new TypeReference<LinkedHashMap<String, Object>>() {
            });
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    public static String writeJson(ObjectMapper mapper, Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }

    public static List<Map<String, Object>> readLyric(ObjectMapper mapper, String json) {
        if (StringUtils.isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static List<Long> splitIds(String raw) {
        List<Long> ids = new ArrayList<>();
        if (StringUtils.isBlank(raw)) {
            return ids;
        }
        for (String part : raw.split(",")) {
            Long id = toLong(part.trim());
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }
}
