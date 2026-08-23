package net.lab1024.sa.admin.module.business.media.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.business.media.dao.MediaCatalogDao;
import net.lab1024.sa.admin.module.business.media.dao.MediaUserActionDao;
import net.lab1024.sa.admin.module.business.media.dao.MusicPlaylistDao;
import net.lab1024.sa.admin.module.business.media.dao.MusicSongDao;
import net.lab1024.sa.admin.module.business.media.domain.entity.MediaCatalogEntity;
import net.lab1024.sa.admin.module.business.media.domain.entity.MediaUserActionEntity;
import net.lab1024.sa.admin.module.business.media.domain.entity.MusicPlaylistEntity;
import net.lab1024.sa.admin.module.business.media.domain.entity.MusicSongEntity;
import net.lab1024.sa.admin.module.business.media.domain.form.MediaQueryForm;
import net.lab1024.sa.admin.util.AdminRequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MusicService {

    @Resource
    private MediaSeedService mediaSeedService;
    @Resource
    private MusicSongDao musicSongDao;
    @Resource
    private MusicPlaylistDao musicPlaylistDao;
    @Resource
    private MediaCatalogDao mediaCatalogDao;
    @Resource
    private MediaUserActionDao mediaUserActionDao;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private MusicOnlineClient musicOnlineClient;

    public Map<String, Object> discover() {
        mediaSeedService.ensureReady();
        Map<String, Object> vo = new LinkedHashMap<>();
        vo.put("banners", catalog(MediaSupport.KIND_MUSIC_BANNER));
        vo.put("playlists", playlists(new MediaQueryForm()));
        vo.put("songs", songs());
        return vo;
    }

    public List<Map<String, Object>> songs() {
        mediaSeedService.ensureReady();
        Set<Long> liked = likedSongIds();
        return musicSongDao.selectList(new LambdaQueryWrapper<MusicSongEntity>()
                        .eq(MusicSongEntity::getDeletedFlag, false)
                        .orderByAsc(MusicSongEntity::getSongId))
                .stream()
                .map(e -> toSong(e, liked))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> playlists(MediaQueryForm form) {
        mediaSeedService.ensureReady();
        String keyword = form == null ? null : form.getKeyword();
        return musicPlaylistDao.selectList(null).stream()
                .filter(e -> StringUtils.isBlank(keyword) || e.getName().contains(keyword))
                .map(this::toPlaylist)
                .collect(Collectors.toList());
    }

    public Map<String, Object> playlistDetail(Long id) {
        mediaSeedService.ensureReady();
        MusicPlaylistEntity entity = musicPlaylistDao.selectById(id);
        if (entity == null) {
            List<Map<String, Object>> list = playlists(new MediaQueryForm());
            return list.isEmpty() ? Map.of("songs", List.of()) : Map.of("playlist", list.get(0), "songs", songs());
        }
        Map<String, Object> vo = new LinkedHashMap<>(toPlaylist(entity));
        List<Long> ids = MediaSupport.splitIds(entity.getSongIds());
        List<Map<String, Object>> all = songs();
        vo.put("songs", ids.isEmpty() ? all : all.stream().filter(s -> ids.contains(MediaSupport.toLong(s.get("songId")))).collect(Collectors.toList()));
        return vo;
    }

    public List<Map<String, Object>> artists() {
        mediaSeedService.ensureReady();
        return catalog(MediaSupport.KIND_ARTIST);
    }

    public Map<String, Object> artistDetail(Long id) {
        Map<String, Object> artist = findCatalog(MediaSupport.KIND_ARTIST, "artistId", id);
        Map<String, Object> vo = new LinkedHashMap<>(artist);
        String name = String.valueOf(artist.getOrDefault("name", ""));
        vo.put("songs", songs().stream().filter(s -> String.valueOf(s.get("artist")).contains(name)).collect(Collectors.toList()));
        return vo;
    }

    public List<Map<String, Object>> albums() {
        mediaSeedService.ensureReady();
        return catalog(MediaSupport.KIND_ALBUM);
    }

    public Map<String, Object> albumDetail(Long id) {
        Map<String, Object> album = findCatalog(MediaSupport.KIND_ALBUM, "albumId", id);
        Map<String, Object> vo = new LinkedHashMap<>(album);
        String name = String.valueOf(album.getOrDefault("name", ""));
        vo.put("songs", songs().stream().filter(s -> name.equals(String.valueOf(s.get("album")))).collect(Collectors.toList()));
        return vo;
    }

    public List<Map<String, Object>> radios() {
        mediaSeedService.ensureReady();
        return catalog(MediaSupport.KIND_RADIO);
    }

    public List<Map<String, Object>> search(String keyword) {
        mediaSeedService.ensureReady();
        if (StringUtils.isBlank(keyword)) {
            return songs();
        }
        List<Map<String, Object>> local = songs().stream()
                .filter(s -> String.valueOf(s.get("name")).contains(keyword)
                        || String.valueOf(s.get("artist")).contains(keyword)
                        || String.valueOf(s.get("album")).contains(keyword))
                .collect(Collectors.toList());
        List<Map<String, Object>> online = persistOnline(musicOnlineClient.search(keyword.trim()));
        LinkedHashMap<String, Map<String, Object>> merged = new LinkedHashMap<>();
        for (Map<String, Object> song : online) {
            merged.put(keyOf(song), song);
        }
        for (Map<String, Object> song : local) {
            merged.putIfAbsent(keyOf(song), song);
        }
        return new ArrayList<>(merged.values());
    }

    public List<Map<String, Object>> favorite() {
        mediaSeedService.ensureReady();
        Set<Long> liked = likedSongIds();
        return songs().stream().filter(s -> liked.contains(MediaSupport.toLong(s.get("songId")))).collect(Collectors.toList());
    }

    public List<Map<String, Object>> recent() {
        mediaSeedService.ensureReady();
        List<Long> ids = actions(MediaSupport.ACTION_RECENT_SONG).stream()
                .sorted((a, b) -> b.getUpdateTime().compareTo(a.getUpdateTime()))
                .map(MediaUserActionEntity::getBizId)
                .collect(Collectors.toList());
        Map<Long, Map<String, Object>> songMap = songs().stream().collect(Collectors.toMap(s -> MediaSupport.toLong(s.get("songId")), s -> s, (a, b) -> a, LinkedHashMap::new));
        return ids.stream().map(songMap::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public boolean toggleLike(Long songId) {
        mediaSeedService.ensureReady();
        Long userId = requireUserId();
        MediaUserActionEntity exist = findAction(userId, MediaSupport.ACTION_LIKE_SONG, songId);
        if (exist != null) {
            mediaUserActionDao.deleteById(exist.getActionId());
            return false;
        }
        MediaUserActionEntity entity = new MediaUserActionEntity();
        entity.setUserId(userId);
        entity.setActionType(MediaSupport.ACTION_LIKE_SONG);
        entity.setBizId(songId);
        entity.setUpdateTime(LocalDateTime.now());
        mediaUserActionDao.insert(entity);
        return true;
    }

    public void reportPlay(Long songId) {
        mediaSeedService.ensureReady();
        Long userId = requireUserId();
        MediaUserActionEntity exist = findAction(userId, MediaSupport.ACTION_RECENT_SONG, songId);
        if (exist == null) {
            MediaUserActionEntity entity = new MediaUserActionEntity();
            entity.setUserId(userId);
            entity.setActionType(MediaSupport.ACTION_RECENT_SONG);
            entity.setBizId(songId);
            entity.setUpdateTime(LocalDateTime.now());
            mediaUserActionDao.insert(entity);
        } else {
            exist.setUpdateTime(LocalDateTime.now());
            mediaUserActionDao.updateById(exist);
        }
    }

    public void stream(Long songId, jakarta.servlet.http.HttpServletResponse response) {
        mediaSeedService.ensureReady();
        MusicSongEntity song = musicSongDao.selectById(songId);
        if (song == null || StringUtils.isBlank(song.getAudioUrl())) {
            throw new IllegalStateException("歌曲不存在或没有可播放地址");
        }
        String target = musicOnlineClient.resolvePlayUrl(song.getAudioUrl(), song.getName(), song.getArtist());
        if (StringUtils.isBlank(target)) {
            throw new IllegalStateException("无法解析播放地址");
        }
        try {
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(target))
                    .timeout(java.time.Duration.ofSeconds(20))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .header("Referer", "https://music.163.com/")
                    .GET()
                    .build();
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                    .connectTimeout(java.time.Duration.ofSeconds(8))
                    .build();
            java.net.http.HttpResponse<java.io.InputStream> remote = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofInputStream());
            if (remote.statusCode() >= 400) {
                throw new IllegalStateException("音源返回 " + remote.statusCode());
            }
            String contentType = remote.headers().firstValue("Content-Type").orElse("audio/mpeg");
            if (contentType.contains("text") || contentType.contains("json")) {
                throw new IllegalStateException("当前曲目无版权或音源不可用，请换一首");
            }
            response.setContentType(contentType);
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Cache-Control", "no-store");
            try (java.io.InputStream in = remote.body(); java.io.OutputStream out = response.getOutputStream()) {
                in.transferTo(out);
                out.flush();
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("播放失败：" + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> lyric(Long songId) {
        mediaSeedService.ensureReady();
        MusicSongEntity song = musicSongDao.selectById(songId);
        if (song == null) {
            return List.of();
        }
        List<Map<String, Object>> stored = MediaSupport.readLyric(objectMapper, song.getLyric());
        if (!stored.isEmpty()) {
            return stored;
        }
        List<Map<String, Object>> online = musicOnlineClient.lyric(song.getAudioUrl());
        if (!online.isEmpty()) {
            song.setLyric(MediaSupport.writeJson(objectMapper, online));
            musicSongDao.updateById(song);
        }
        return online;
    }

    private List<Map<String, Object>> persistOnline(List<Map<String, Object>> online) {
        Set<Long> liked = likedSongIds();
        List<Map<String, Object>> saved = new ArrayList<>();
        for (Map<String, Object> item : online) {
            String name = String.valueOf(item.getOrDefault("name", "")).trim();
            String artist = String.valueOf(item.getOrDefault("artist", "")).trim();
            if (StringUtils.isBlank(name)) {
                continue;
            }
            MusicSongEntity exist = musicSongDao.selectOne(new LambdaQueryWrapper<MusicSongEntity>()
                    .eq(MusicSongEntity::getDeletedFlag, false)
                    .eq(MusicSongEntity::getName, name)
                    .eq(MusicSongEntity::getArtist, artist)
                    .last("limit 1"));
            if (exist == null) {
                exist = new MusicSongEntity();
                exist.setName(name);
                exist.setArtist(artist);
                exist.setAlbum(String.valueOf(item.getOrDefault("album", "")));
                exist.setDuration(String.valueOf(item.getOrDefault("duration", "00:00")));
                exist.setAudioUrl(String.valueOf(item.getOrDefault("audioUrl", "")));
                exist.setCoverUrl(String.valueOf(item.getOrDefault("coverUrl", "")));
                exist.setDeletedFlag(false);
                musicSongDao.insert(exist);
            } else if (StringUtils.isBlank(exist.getAudioUrl())) {
                exist.setAudioUrl(String.valueOf(item.getOrDefault("audioUrl", "")));
                exist.setCoverUrl(String.valueOf(item.getOrDefault("coverUrl", exist.getCoverUrl())));
                musicSongDao.updateById(exist);
            }
            saved.add(toSong(exist, liked));
        }
        return saved;
    }

    private String keyOf(Map<String, Object> song) {
        return String.valueOf(song.get("name")) + "#" + String.valueOf(song.get("artist"));
    }

    private Set<Long> likedSongIds() {
        Long userId = AdminRequestUtil.getRequestUserId();
        if (userId == null) {
            return Set.of();
        }
        return actions(MediaSupport.ACTION_LIKE_SONG).stream().map(MediaUserActionEntity::getBizId).collect(Collectors.toSet());
    }

    private List<MediaUserActionEntity> actions(String type) {
        Long userId = AdminRequestUtil.getRequestUserId();
        if (userId == null) {
            return List.of();
        }
        return mediaUserActionDao.selectList(new LambdaQueryWrapper<MediaUserActionEntity>()
                .eq(MediaUserActionEntity::getUserId, userId)
                .eq(MediaUserActionEntity::getActionType, type));
    }

    private MediaUserActionEntity findAction(Long userId, String type, Long bizId) {
        return mediaUserActionDao.selectOne(new LambdaQueryWrapper<MediaUserActionEntity>()
                .eq(MediaUserActionEntity::getUserId, userId)
                .eq(MediaUserActionEntity::getActionType, type)
                .eq(MediaUserActionEntity::getBizId, bizId)
                .last("limit 1"));
    }

    private Long requireUserId() {
        Long userId = AdminRequestUtil.getRequestUserId();
        if (userId == null) {
            throw new IllegalStateException("请先登录后再使用媒体中心");
        }
        return userId;
    }

    private List<Map<String, Object>> catalog(String kind) {
        return mediaCatalogDao.selectList(new LambdaQueryWrapper<MediaCatalogEntity>()
                        .eq(MediaCatalogEntity::getKind, kind)
                        .eq(MediaCatalogEntity::getDeletedFlag, false)
                        .orderByAsc(MediaCatalogEntity::getCatalogId))
                .stream()
                .map(e -> MediaSupport.readMap(objectMapper, e.getPayload()))
                .collect(Collectors.toList());
    }

    private Map<String, Object> findCatalog(String kind, String field, Long id) {
        return catalog(kind).stream()
                .filter(e -> id != null && id.equals(MediaSupport.toLong(e.get(field))))
                .findFirst()
                .orElseGet(() -> {
                    List<Map<String, Object>> list = catalog(kind);
                    return list.isEmpty() ? new LinkedHashMap<>() : list.get(0);
                });
    }

    private Map<String, Object> toSong(MusicSongEntity e, Set<Long> liked) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("songId", e.getSongId());
        map.put("name", e.getName());
        map.put("artist", e.getArtist());
        map.put("album", e.getAlbum());
        map.put("duration", e.getDuration());
        map.put("audioUrl", e.getAudioUrl());
        map.put("coverUrl", e.getCoverUrl());
        map.put("liked", liked.contains(e.getSongId()));
        map.put("lyrics", MediaSupport.readLyric(objectMapper, e.getLyric()));
        return map;
    }

    private Map<String, Object> toPlaylist(MusicPlaylistEntity e) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("playlistId", e.getPlaylistId());
        map.put("name", e.getName());
        map.put("playCount", e.getPlayCount());
        map.put("songCount", e.getSongCount());
        map.put("coverUrl", e.getCoverUrl());
        map.put("description", e.getDescription());
        map.put("songIds", e.getSongIds());
        return map;
    }
}
