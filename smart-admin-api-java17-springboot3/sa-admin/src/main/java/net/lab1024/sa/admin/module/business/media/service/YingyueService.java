package net.lab1024.sa.admin.module.business.media.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.business.media.dao.MediaCatalogDao;
import net.lab1024.sa.admin.module.business.media.dao.MediaUserActionDao;
import net.lab1024.sa.admin.module.business.media.dao.VideoDao;
import net.lab1024.sa.admin.module.business.media.dao.VideoHistoryDao;
import net.lab1024.sa.admin.module.business.media.domain.entity.MediaCatalogEntity;
import net.lab1024.sa.admin.module.business.media.domain.entity.MediaUserActionEntity;
import net.lab1024.sa.admin.module.business.media.domain.entity.VideoEntity;
import net.lab1024.sa.admin.module.business.media.domain.entity.VideoHistoryEntity;
import net.lab1024.sa.admin.module.business.media.domain.form.MediaQueryForm;
import net.lab1024.sa.admin.util.AdminRequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class YingyueService {

    @Resource
    private MediaSeedService mediaSeedService;
    @Resource
    private VideoDao videoDao;
    @Resource
    private VideoHistoryDao videoHistoryDao;
    @Resource
    private MediaCatalogDao mediaCatalogDao;
    @Resource
    private MediaUserActionDao mediaUserActionDao;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private YingyueOnlineClient yingyueOnlineClient;

    public Map<String, Object> home() {
        mediaSeedService.ensureReady();
        persistOnline(yingyueOnlineClient.trending());
        Map<String, Object> vo = new LinkedHashMap<>();
        List<Map<String, Object>> all = videos();
        vo.put("banners", bannersFrom(all));
        vo.put("history", history());
        vo.put("videos", all);
        return vo;
    }

    public List<Map<String, Object>> videos() {
        mediaSeedService.ensureReady();
        return videoDao.selectList(new LambdaQueryWrapper<VideoEntity>()
                        .eq(VideoEntity::getDeletedFlag, false)
                        .orderByAsc(VideoEntity::getVideoId))
                .stream()
                .map(this::toVideo)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> library(MediaQueryForm form) {
        mediaSeedService.ensureReady();
        Integer category = form == null ? null : (form.getCategory() != null ? form.getCategory() : form.getScene());
        String area = form == null ? null : form.getArea();
        return videos().stream()
                .filter(e -> category == null || category == 0 || Objects.equals(MediaSupport.toInt(e.get("category")), category))
                .filter(e -> StringUtils.isBlank(area) || area.equals(String.valueOf(e.get("area"))))
                .collect(Collectors.toList());
    }

    public Map<String, Object> detail(Long id) {
        mediaSeedService.ensureReady();
        VideoEntity entity = videoDao.selectById(id);
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) {
            List<Map<String, Object>> list = videos();
            return list.isEmpty() ? new LinkedHashMap<>() : list.get(0);
        }
        Map<String, Object> vo = toVideo(entity);
        vo.put("favorite", favoriteIds().contains(entity.getVideoId()));
        return vo;
    }

    public List<Map<String, Object>> search(String keyword) {
        mediaSeedService.ensureReady();
        if (StringUtils.isBlank(keyword)) {
            return videos();
        }
        List<Map<String, Object>> online = persistOnline(yingyueOnlineClient.search(keyword.trim()));
        List<Map<String, Object>> local = videos().stream()
                .filter(e -> String.valueOf(e.get("title")).contains(keyword) || String.valueOf(e.get("intro")).contains(keyword))
                .collect(Collectors.toList());
        LinkedHashMap<Long, Map<String, Object>> merged = new LinkedHashMap<>();
        online.forEach(item -> merged.put(MediaSupport.toLong(item.get("videoId")), item));
        local.forEach(item -> merged.putIfAbsent(MediaSupport.toLong(item.get("videoId")), item));
        return new ArrayList<>(merged.values());
    }

    public List<Map<String, Object>> favorite() {
        mediaSeedService.ensureReady();
        Set<Long> ids = favoriteIds();
        return videos().stream().filter(e -> ids.contains(MediaSupport.toLong(e.get("videoId")))).collect(Collectors.toList());
    }

    public List<Map<String, Object>> history() {
        mediaSeedService.ensureReady();
        Long userId = AdminRequestUtil.getRequestUserId();
        if (userId == null) {
            return List.of();
        }
        Map<Long, Map<String, Object>> videoMap = videos().stream()
                .collect(Collectors.toMap(e -> MediaSupport.toLong(e.get("videoId")), e -> e, (a, b) -> a));
        return videoHistoryDao.selectList(new LambdaQueryWrapper<VideoHistoryEntity>()
                        .eq(VideoHistoryEntity::getUserId, userId)
                        .orderByDesc(VideoHistoryEntity::getUpdateTime))
                .stream()
                .map(h -> {
                    Map<String, Object> video = videoMap.get(h.getVideoId());
                    if (video == null) {
                        return null;
                    }
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("videoId", h.getVideoId());
                    row.put("title", video.get("title"));
                    row.put("coverUrl", video.get("coverUrl"));
                    row.put("episodeNo", h.getEpisodeNo());
                    row.put("progress", h.getProgress());
                    row.put("updateTime", MediaSupport.formatTime(h.getUpdateTime()));
                    return row;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public boolean toggleFavorite(Long videoId) {
        mediaSeedService.ensureReady();
        Long userId = requireUserId();
        MediaUserActionEntity exist = mediaUserActionDao.selectOne(new LambdaQueryWrapper<MediaUserActionEntity>()
                .eq(MediaUserActionEntity::getUserId, userId)
                .eq(MediaUserActionEntity::getActionType, MediaSupport.ACTION_FAVORITE_VIDEO)
                .eq(MediaUserActionEntity::getBizId, videoId)
                .last("limit 1"));
        if (exist != null) {
            mediaUserActionDao.deleteById(exist.getActionId());
            return false;
        }
        MediaUserActionEntity entity = new MediaUserActionEntity();
        entity.setUserId(userId);
        entity.setActionType(MediaSupport.ACTION_FAVORITE_VIDEO);
        entity.setBizId(videoId);
        entity.setUpdateTime(LocalDateTime.now());
        mediaUserActionDao.insert(entity);
        return true;
    }

    public void reportHistory(Map<String, Object> body) {
        mediaSeedService.ensureReady();
        Long userId = requireUserId();
        Long videoId = MediaSupport.toLong(body.get("videoId"));
        if (videoId == null) {
            return;
        }
        Integer episodeNo = MediaSupport.toInt(body.getOrDefault("episodeNo", 1));
        Integer progress = MediaSupport.toInt(body.getOrDefault("progress", 0));
        VideoHistoryEntity exist = videoHistoryDao.selectOne(new LambdaQueryWrapper<VideoHistoryEntity>()
                .eq(VideoHistoryEntity::getUserId, userId)
                .eq(VideoHistoryEntity::getVideoId, videoId)
                .last("limit 1"));
        if (exist == null) {
            VideoHistoryEntity entity = new VideoHistoryEntity();
            entity.setUserId(userId);
            entity.setVideoId(videoId);
            entity.setEpisodeNo(episodeNo);
            entity.setProgress(progress);
            entity.setUpdateTime(LocalDateTime.now());
            videoHistoryDao.insert(entity);
        } else {
            exist.setEpisodeNo(episodeNo);
            exist.setProgress(progress);
            exist.setUpdateTime(LocalDateTime.now());
            videoHistoryDao.updateById(exist);
        }
    }

    public void stream(Long videoId, jakarta.servlet.http.HttpServletResponse response) {
        mediaSeedService.ensureReady();
        VideoEntity video = videoDao.selectById(videoId);
        List<String> candidates = new ArrayList<>();
        if (video != null && StringUtils.isNotBlank(video.getPlayUrl()) && !video.getPlayUrl().contains("googleapis")) {
            candidates.add(yingyueOnlineClient.resolvePlayUrl(video.getPlayUrl()));
        }
        candidates.addAll(List.of(MediaSupport.VIDEO));
        Exception last = null;
        for (String target : candidates) {
            try {
                proxyVideo(target, response);
                return;
            } catch (Exception e) {
                last = e;
            }
        }
        throw new IllegalStateException("播放失败：" + (last == null ? "无可用片源" : last.getMessage()), last);
    }

    private void proxyVideo(String target, jakarta.servlet.http.HttpServletResponse response) throws Exception {
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(target))
                .timeout(java.time.Duration.ofSeconds(12))
                .header("User-Agent", "Mozilla/5.0")
                .GET()
                .build();
        java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                .connectTimeout(java.time.Duration.ofSeconds(5))
                .build();
        java.net.http.HttpResponse<java.io.InputStream> remote = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofInputStream());
        if (remote.statusCode() >= 400) {
            throw new IllegalStateException("片源返回 " + remote.statusCode());
        }
        String contentType = remote.headers().firstValue("Content-Type").orElse("video/mp4");
        if (contentType.contains("text") || contentType.contains("json") || contentType.contains("html")) {
            throw new IllegalStateException("片源不可播");
        }
        response.setContentType(contentType.contains("video") || contentType.contains("mp4") ? contentType : "video/mp4");
        response.setHeader("Cache-Control", "no-store");
        try (java.io.InputStream in = remote.body(); java.io.OutputStream out = response.getOutputStream()) {
            in.transferTo(out);
            out.flush();
        }
    }

    public List<Map<String, Object>> rank() {
        persistOnline(yingyueOnlineClient.trending());
        return videos().stream()
                .sorted(Comparator.comparing((Map<String, Object> e) -> {
                    Object score = e.get("score");
                    return score == null ? 0d : Double.parseDouble(String.valueOf(score));
                }).reversed())
                .collect(Collectors.toList());
    }

    private Set<Long> favoriteIds() {
        Long userId = AdminRequestUtil.getRequestUserId();
        if (userId == null) {
            return Set.of();
        }
        return mediaUserActionDao.selectList(new LambdaQueryWrapper<MediaUserActionEntity>()
                        .eq(MediaUserActionEntity::getUserId, userId)
                        .eq(MediaUserActionEntity::getActionType, MediaSupport.ACTION_FAVORITE_VIDEO))
                .stream()
                .map(MediaUserActionEntity::getBizId)
                .collect(Collectors.toSet());
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

    private List<Map<String, Object>> persistOnline(List<Map<String, Object>> online) {
        List<Map<String, Object>> saved = new ArrayList<>();
        for (Map<String, Object> item : online) {
            String title = String.valueOf(item.getOrDefault("title", "")).trim();
            String source = String.valueOf(item.getOrDefault("source", ""));
            String sourceId = String.valueOf(item.getOrDefault("sourceId", ""));
            if (StringUtils.isBlank(title) || StringUtils.isBlank(item.getOrDefault("playUrl", "").toString())) {
                continue;
            }
            VideoEntity exist = null;
            if (StringUtils.isNotBlank(source) && StringUtils.isNotBlank(sourceId)) {
                exist = videoDao.selectOne(new LambdaQueryWrapper<VideoEntity>()
                        .eq(VideoEntity::getDeletedFlag, false)
                        .eq(VideoEntity::getSource, source)
                        .eq(VideoEntity::getSourceId, sourceId)
                        .last("limit 1"));
            }
            if (exist == null) {
                exist = videoDao.selectOne(new LambdaQueryWrapper<VideoEntity>()
                        .eq(VideoEntity::getDeletedFlag, false)
                        .eq(VideoEntity::getTitle, title)
                        .last("limit 1"));
            }
            if (exist == null) {
                exist = new VideoEntity();
                exist.setTitle(title);
                exist.setCategory(MediaSupport.toInt(item.getOrDefault("category", 1)));
                exist.setArea(String.valueOf(item.getOrDefault("area", "us")));
                exist.setYear(MediaSupport.toInt(item.get("year")));
                Object score = item.get("score");
                if (score instanceof java.math.BigDecimal decimal) {
                    exist.setScore(decimal);
                }
                exist.setEpisodeCount(MediaSupport.toInt(item.getOrDefault("episodeCount", 1)));
                exist.setUpdateInfo(String.valueOf(item.getOrDefault("updateInfo", "正片")));
                exist.setPlayUrl(String.valueOf(item.get("playUrl")));
                exist.setCoverUrl(String.valueOf(item.get("coverUrl")));
                exist.setIntro(String.valueOf(item.getOrDefault("intro", title)));
                exist.setSource(source);
                exist.setSourceId(sourceId);
                exist.setDeletedFlag(false);
                videoDao.insert(exist);
            } else if (StringUtils.isBlank(exist.getPlayUrl()) || exist.getPlayUrl().contains("googleapis")) {
                exist.setPlayUrl(String.valueOf(item.get("playUrl")));
                exist.setCoverUrl(String.valueOf(item.getOrDefault("coverUrl", exist.getCoverUrl())));
                exist.setSource(source);
                exist.setSourceId(sourceId);
                videoDao.updateById(exist);
            }
            saved.add(toVideo(exist));
        }
        return saved;
    }

    private List<Map<String, Object>> bannersFrom(List<Map<String, Object>> videos) {
        List<Map<String, Object>> banners = catalog(MediaSupport.KIND_YINGYUE_BANNER);
        if (videos.size() >= 3) {
            List<Map<String, Object>> online = videos.stream().limit(3).map(item -> {
                Map<String, Object> banner = new LinkedHashMap<>();
                banner.put("videoId", item.get("videoId"));
                banner.put("title", item.get("title"));
                banner.put("subTitle", item.get("intro"));
                banner.put("coverUrl", item.get("coverUrl"));
                return banner;
            }).collect(Collectors.toList());
            return online;
        }
        return banners;
    }

    private Map<String, Object> toVideo(VideoEntity e) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("videoId", e.getVideoId());
        map.put("title", e.getTitle());
        map.put("category", e.getCategory());
        map.put("area", e.getArea());
        map.put("year", e.getYear());
        map.put("score", e.getScore());
        map.put("episodeCount", e.getEpisodeCount());
        map.put("updateInfo", e.getUpdateInfo());
        map.put("playUrl", e.getPlayUrl());
        map.put("coverUrl", e.getCoverUrl());
        map.put("intro", e.getIntro());
        map.put("source", e.getSource());
        return map;
    }
}
