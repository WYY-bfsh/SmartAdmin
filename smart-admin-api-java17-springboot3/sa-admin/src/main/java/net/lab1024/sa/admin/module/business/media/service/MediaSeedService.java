package net.lab1024.sa.admin.module.business.media.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.media.dao.AiMaterialDao;
import net.lab1024.sa.admin.module.business.media.dao.AiProjectDao;
import net.lab1024.sa.admin.module.business.media.dao.AiTaskDao;
import net.lab1024.sa.admin.module.business.media.dao.MediaCatalogDao;
import net.lab1024.sa.admin.module.business.media.dao.MusicPlaylistDao;
import net.lab1024.sa.admin.module.business.media.dao.MusicSongDao;
import net.lab1024.sa.admin.module.business.media.dao.VideoDao;
import net.lab1024.sa.admin.module.business.media.domain.entity.AiMaterialEntity;
import net.lab1024.sa.admin.module.business.media.domain.entity.AiProjectEntity;
import net.lab1024.sa.admin.module.business.media.domain.entity.AiTaskEntity;
import net.lab1024.sa.admin.module.business.media.domain.entity.MediaCatalogEntity;
import net.lab1024.sa.admin.module.business.media.domain.entity.MusicPlaylistEntity;
import net.lab1024.sa.admin.module.business.media.domain.entity.MusicSongEntity;
import net.lab1024.sa.admin.module.business.media.domain.entity.VideoEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 首次访问时把公网 CDN 片源写入数据库。播放与封面必须联网。
 */
@Slf4j
@Service
public class MediaSeedService {

    @Resource
    private AiProjectDao aiProjectDao;
    @Resource
    private AiMaterialDao aiMaterialDao;
    @Resource
    private AiTaskDao aiTaskDao;
    @Resource
    private MusicSongDao musicSongDao;
    @Resource
    private MusicPlaylistDao musicPlaylistDao;
    @Resource
    private VideoDao videoDao;
    @Resource
    private MediaCatalogDao mediaCatalogDao;
    @Resource
    private MediaSchemaService mediaSchemaService;
    @Resource
    private ObjectMapper objectMapper;

    private volatile boolean ready;

    public void ensureReady() {
        if (ready) {
            return;
        }
        synchronized (this) {
            if (ready) {
                return;
            }
            try {
                mediaSchemaService.ensureTables();
                seedProjects();
                seedMaterials();
                seedTasks();
                seedSongs();
                seedPlaylists();
                seedVideos();
                seedCatalog();
                ready = true;
            } catch (Exception e) {
                log.error("媒体中心初始化失败", e);
                String cause = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                throw new IllegalStateException("媒体中心初始化失败：" + cause, e);
            }
        }
    }

    private void seedProjects() {
        if (aiProjectDao.selectCount(null) > 0) {
            return;
        }
        insertProject("夏日漫剪·光与影", 4, 30, "00:45", "9:16", "ai1");
        insertProject("口播带货 60 秒", 1, 20, "01:00", "9:16", "ai2");
        insertProject("知识区图文快剪", 2, 10, "02:18", "16:9", "ai3");
        insertProject("城市夜景 Vlog", 3, 10, "00:28", "9:16", "ai4");
        insertProject("热点资讯快剪", 5, 30, "00:36", "9:16", "ai5");
        insertProject("二次元混剪 OP", 4, 40, "01:32", "16:9", "ai6");
    }

    private void insertProject(String title, int scene, int status, String duration, String ratio, String seed) {
        AiProjectEntity e = new AiProjectEntity();
        e.setTitle(title);
        e.setScene(scene);
        e.setStatus(status);
        e.setDuration(duration);
        e.setRatio(ratio);
        e.setCoverUrl(MediaSupport.cover(seed));
        e.setDeletedFlag(false);
        aiProjectDao.insert(e);
    }

    private void seedMaterials() {
        if (aiMaterialDao.selectCount(null) > 0) {
            return;
        }
        insertMaterial("海边延时.mp4", 1, "00:12", "18.2MB", MediaSupport.VIDEO[0], "m1");
        insertMaterial("夜景车流.mp4", 1, "00:08", "9.4MB", MediaSupport.VIDEO[1], "m2");
        insertMaterial("人物口播.mp4", 1, "01:05", "42.0MB", MediaSupport.VIDEO[2], "m3");
        insertMaterial("产品特写.jpg", 2, "-", "2.1MB", MediaSupport.cover("m4", 320, 320), "m4");
        insertMaterial("封面海报.png", 2, "-", "1.6MB", MediaSupport.cover("m5", 320, 320), "m5");
        insertMaterial("轻快 BGM.mp3", 3, "02:40", "4.8MB", MediaSupport.AUDIO[0], "m6");
        insertMaterial("情绪钢琴.mp3", 3, "03:12", "5.5MB", MediaSupport.AUDIO[1], "m7");
    }

    private void insertMaterial(String name, int type, String duration, String size, String fileUrl, String seed) {
        AiMaterialEntity e = new AiMaterialEntity();
        e.setName(name);
        e.setType(type);
        e.setDuration(duration);
        e.setSize(size);
        e.setCoverUrl(MediaSupport.cover(seed, type == 1 ? 320 : 320, type == 1 ? 180 : 320));
        e.setFileUrl(fileUrl);
        e.setDeletedFlag(false);
        aiMaterialDao.insert(e);
    }

    private void seedTasks() {
        if (aiTaskDao.selectCount(null) > 0) {
            return;
        }
        insertTask("夏日漫剪·光与影", "一键成片", 100, 30, MediaSupport.VIDEO[0]);
        insertTask("口播带货 60 秒", "口播成片", 62, 20, null);
        insertTask("二次元混剪 OP", "踩点成片", 0, 40, null);
        insertTask("知识区图文快剪", "图文成片", 0, 10, null);
    }

    private void insertTask(String title, String type, int progress, int status, String resultUrl) {
        AiTaskEntity e = new AiTaskEntity();
        e.setTitle(title);
        e.setType(type);
        e.setProgress(progress);
        e.setStatus(status);
        e.setResultUrl(resultUrl);
        e.setDeletedFlag(false);
        aiTaskDao.insert(e);
    }

    private void seedSongs() {
        if (musicSongDao.selectCount(null) > 0) {
            return;
        }
        insertSong("海风与晚霞", "林栖", "南岛日记", "03:42", MediaSupport.AUDIO[0], "s1", lyric("海风把白天吹旧了", "晚霞停在你的肩头", "我们谁都不必开口", "潮水会记住这一首"));
        insertSong("午夜电台", "北辰", "城市回声", "04:08", MediaSupport.AUDIO[1], "s2", lyric("午夜电台还在播", "城市把灯都关了", "只有耳机里的人", "陪我走完这一段"));
        insertSong("未完成的信", "苏晚", "纸飞机", "03:21", MediaSupport.AUDIO[2], "s3", lyric("信纸叠成纸飞机", "飞过没说完的夜", "如果还能被你拆开", "请把它读成再见"));
        insertSong("山海之间", "阿远", "山海之间", "04:55", MediaSupport.AUDIO[3], "s4", lyric("山在左边海在右", "路在尘土里开口", "我把行李放进风", "把名字留给路口"));
        insertSong("咖啡店的雨", "林栖 / 苏晚", "双人漫步", "03:36", MediaSupport.AUDIO[4], "s5", lyric("窗外的雨敲杯子", "我们把时间煮浓", "这一首歌不要副歌", "只要你坐在对面"));
        insertSong("轨道", "北辰", "城市回声", "02:58", MediaSupport.AUDIO[0], "s6", lyric("末班车还亮着灯", "轨道把城市切开", "我站在两站之间", "决定去或不去"));
        insertSong("白夜飞行", "苏晚", "纸飞机", "03:47", MediaSupport.AUDIO[1], "s7", lyric("白天不肯结束", "夜色学会飞行", "把窗户留一条缝", "让风进来找你"));
        insertSong("旧胶片", "阿远", "山海之间", "04:12", MediaSupport.AUDIO[2], "s8", lyric("旧胶片还在转", "灰尘落在笑脸上", "我倒带回原点", "发现你还在场"));
    }

    private String lyric(String... lines) {
        List<Map<String, Object>> list = new java.util.ArrayList<>();
        int time = 0;
        for (String line : lines) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("time", time);
            row.put("text", line);
            list.add(row);
            time += 8;
        }
        return MediaSupport.writeJson(objectMapper, list);
    }

    private void insertSong(String name, String artist, String album, String duration, String audio, String seed, String lyric) {
        MusicSongEntity e = new MusicSongEntity();
        e.setName(name);
        e.setArtist(artist);
        e.setAlbum(album);
        e.setDuration(duration);
        e.setAudioUrl(audio);
        e.setCoverUrl(MediaSupport.cover(seed, 240, 240));
        e.setLyric(lyric);
        e.setDeletedFlag(false);
        musicSongDao.insert(e);
    }

    private void seedPlaylists() {
        if (musicPlaylistDao.selectCount(null) > 0) {
            return;
        }
        insertPlaylist("夜听华语", "128万", 28, "适合加班后戴上耳机的温柔歌单。", "p1", "1,3,5,7");
        insertPlaylist("通勤提神", "86万", 32, "节奏明快，一站到公司刚刚好。", "p2", "2,4,6,8");
        insertPlaylist("安静写作", "210万", 20, "人声克制，只留旋律陪你敲字。", "p3", "1,3,6,8");
        insertPlaylist("周末公路", "64万", 24, "把车窗摇下来，把夏天摇进来。", "p4", "4,5,7,8");
        insertPlaylist("失眠电台", "95万", 18, "凌晨两点还亮着的那盏灯。", "p5", "2,3,6,7");
        insertPlaylist("国风新声", "150万", 26, "古意新唱，弦与鼓同行。", "p6", "1,4,5,8");
    }

    private void insertPlaylist(String name, String playCount, int songCount, String desc, String seed, String songIds) {
        MusicPlaylistEntity e = new MusicPlaylistEntity();
        e.setName(name);
        e.setPlayCount(playCount);
        e.setSongCount(songCount);
        e.setCoverUrl(MediaSupport.cover(seed, 300, 300));
        e.setDescription(desc);
        e.setSongIds(songIds);
        musicPlaylistDao.insert(e);
    }

    private void seedVideos() {
        if (videoDao.selectCount(null) > 0) {
            return;
        }
        insertVideo("北境守夜人", 1, "cn", 2026, "8.7", 1, "正片", MediaSupport.VIDEO[0], "v1", "极北灯塔守夜人发现冰层下的古老信号，一场关于守望与告别的故事。");
        insertVideo("旧城来信", 1, "cn", 2025, "8.1", 1, "正片", MediaSupport.VIDEO[1], "v2", "一封迟到十二年的信，把两个陌生人重新连在一起。");
        insertVideo("雨巷侦探", 2, "cn", 2026, "8.4", 16, "更新至 12 集", MediaSupport.VIDEO[2], "v3", "民国雨巷里的私家侦探，专接没人愿意碰的案子。");
        insertVideo("星海归途", 1, "us", 2024, "9.0", 1, "正片", MediaSupport.VIDEO[3], "v4", "飞船失联七年后突然出现，船员只记得回家这一件事。");
        insertVideo("东京夜行", 2, "jp", 2025, "8.2", 10, "全 10 集", MediaSupport.VIDEO[4], "v5", "出租车司机每晚载着一个只在后视镜里存在的乘客。");
        insertVideo("汉江旁的约定", 2, "kr", 2026, "7.9", 12, "更新至 8 集", MediaSupport.VIDEO[0], "v6", "两个错过十年的人，在汉江边重新学习如何见面。");
        insertVideo("夏日放送", 3, "cn", 2026, "7.6", 12, "更新至 6 期", MediaSupport.VIDEO[1], "v7", "六组嘉宾在海边完成不可能的夏季挑战。");
        insertVideo("灵笼纪元", 4, "cn", 2026, "9.2", 24, "更新至 18 集", MediaSupport.VIDEO[2], "v8", "末世地下城，少年接过上一代没走完的火种。");
        insertVideo("深海档案", 5, "us", 2023, "8.8", 6, "全 6 集", MediaSupport.VIDEO[3], "v9", "跟随科考船，看见人类从未真正到达的地方。");
        insertVideo("京都花见", 1, "jp", 2022, "8.0", 1, "正片", MediaSupport.VIDEO[4], "v10", "樱花季的三天，足够改变一个人的去向。");
    }

    private void insertVideo(String title, int category, String area, int year, String score, int episodes, String updateInfo, String playUrl, String seed, String intro) {
        VideoEntity e = new VideoEntity();
        e.setTitle(title);
        e.setCategory(category);
        e.setArea(area);
        e.setYear(year);
        e.setScore(new BigDecimal(score));
        e.setEpisodeCount(episodes);
        e.setUpdateInfo(updateInfo);
        e.setPlayUrl(playUrl);
        e.setCoverUrl(MediaSupport.cover(seed, 360, 500));
        e.setIntro(intro);
        e.setDeletedFlag(false);
        videoDao.insert(e);
    }

    private void seedCatalog() {
        if (mediaCatalogDao.selectCount(new LambdaQueryWrapper<MediaCatalogEntity>().eq(MediaCatalogEntity::getKind, MediaSupport.KIND_TEMPLATE)) > 0) {
            return;
        }
        insertCatalog(MediaSupport.KIND_TEMPLATE, map("templateId", 1, "name", "爆款口播三段式", "scene", 1, "useCount", 12890, "duration", "00:45", "coverUrl", MediaSupport.cover("t1")));
        insertCatalog(MediaSupport.KIND_TEMPLATE, map("templateId", 2, "name", "知识区字幕卡点", "scene", 2, "useCount", 8760, "duration", "01:20", "coverUrl", MediaSupport.cover("t2")));
        insertCatalog(MediaSupport.KIND_TEMPLATE, map("templateId", 3, "name", "旅行 Vlog 转场", "scene", 3, "useCount", 6540, "duration", "00:30", "coverUrl", MediaSupport.cover("t3")));
        insertCatalog(MediaSupport.KIND_TEMPLATE, map("templateId", 4, "name", "二次元踩点混剪", "scene", 4, "useCount", 20311, "duration", "01:00", "coverUrl", MediaSupport.cover("t4")));
        insertCatalog(MediaSupport.KIND_TEMPLATE, map("templateId", 5, "name", "资讯快讯片头", "scene", 5, "useCount", 3210, "duration", "00:15", "coverUrl", MediaSupport.cover("t5")));
        insertCatalog(MediaSupport.KIND_TEMPLATE, map("templateId", 6, "name", "带货产品特写", "scene", 1, "useCount", 9980, "duration", "00:25", "coverUrl", MediaSupport.cover("t6")));

        insertCatalog(MediaSupport.KIND_SCRIPT, map("scriptId", 1, "title", "夏季防晒口播", "scene", 1, "wordCount", 186, "updateTime", "2026-08-22 10:00", "content", "夏天到了，紫外线变强。这支防晒乳轻薄不搓泥，上脸就像一层水膜。"));
        insertCatalog(MediaSupport.KIND_SCRIPT, map("scriptId", 2, "title", "三步学会时间管理", "scene", 2, "wordCount", 240, "updateTime", "2026-08-21 15:20", "content", "第一步写下三件最重要的事；第二步给每件事定 25 分钟；第三步晚上复盘。"));
        insertCatalog(MediaSupport.KIND_SCRIPT, map("scriptId", 3, "title", "角色名场面混剪旁白", "scene", 4, "wordCount", 92, "updateTime", "2026-08-20 21:00", "content", "如果再来一次，我依然会站在这里。光穿过雨幕，故事才刚刚开始。"));

        insertCatalog(MediaSupport.KIND_ARTIST, map("artistId", 1, "name", "林栖", "fans", "128万", "albumCount", 4, "songCount", 36, "coverUrl", MediaSupport.cover("a1", 300, 300), "intro", "独立唱作人，擅长民谣与城市流行。"));
        insertCatalog(MediaSupport.KIND_ARTIST, map("artistId", 2, "name", "北辰", "fans", "86万", "albumCount", 3, "songCount", 22, "coverUrl", MediaSupport.cover("a2", 300, 300), "intro", "电子流行制作人，作品多用于影视。"));
        insertCatalog(MediaSupport.KIND_ARTIST, map("artistId", 3, "name", "苏晚", "fans", "210万", "albumCount", 5, "songCount", 48, "coverUrl", MediaSupport.cover("a3", 300, 300), "intro", "嗓音空灵，代表作《未完成的信》。"));
        insertCatalog(MediaSupport.KIND_ARTIST, map("artistId", 4, "name", "阿远", "fans", "64万", "albumCount", 2, "songCount", 16, "coverUrl", MediaSupport.cover("a4", 300, 300), "intro", "民谣旅行者，录音棚常年在路上。"));

        insertCatalog(MediaSupport.KIND_ALBUM, map("albumId", 1, "name", "南岛日记", "artist", "林栖", "year", 2025, "songCount", 10, "coverUrl", MediaSupport.cover("al1", 300, 300)));
        insertCatalog(MediaSupport.KIND_ALBUM, map("albumId", 2, "name", "城市回声", "artist", "北辰", "year", 2024, "songCount", 8, "coverUrl", MediaSupport.cover("al2", 300, 300)));
        insertCatalog(MediaSupport.KIND_ALBUM, map("albumId", 3, "name", "纸飞机", "artist", "苏晚", "year", 2026, "songCount", 12, "coverUrl", MediaSupport.cover("al3", 300, 300)));
        insertCatalog(MediaSupport.KIND_ALBUM, map("albumId", 4, "name", "山海之间", "artist", "阿远", "year", 2023, "songCount", 9, "coverUrl", MediaSupport.cover("al4", 300, 300)));

        insertCatalog(MediaSupport.KIND_RADIO, map("radioId", 1, "name", "午夜情感电台", "host", "苏晚", "playing", "今天也想被温柔对待", "coverUrl", MediaSupport.cover("r1", 300, 300)));
        insertCatalog(MediaSupport.KIND_RADIO, map("radioId", 2, "name", "开工提神电波", "host", "北辰", "playing", "周一生存指南", "coverUrl", MediaSupport.cover("r2", 300, 300)));
        insertCatalog(MediaSupport.KIND_RADIO, map("radioId", 3, "name", "故事与民谣", "host", "阿远", "playing", "路过一座无名小站", "coverUrl", MediaSupport.cover("r3", 300, 300)));

        insertCatalog(MediaSupport.KIND_MUSIC_BANNER, map("id", 1, "title", "本周新碟推荐", "coverUrl", MediaSupport.cover("mb1", 960, 320)));
        insertCatalog(MediaSupport.KIND_MUSIC_BANNER, map("id", 2, "title", "华语金曲回顾", "coverUrl", MediaSupport.cover("mb2", 960, 320)));
        insertCatalog(MediaSupport.KIND_MUSIC_BANNER, map("id", 3, "title", "专注时刻 · 纯音乐", "coverUrl", MediaSupport.cover("mb3", 960, 320)));

        insertCatalog(MediaSupport.KIND_YINGYUE_BANNER, map("videoId", 1, "title", "北境守夜人", "subTitle", "风雪里的最后一座灯塔", "coverUrl", MediaSupport.cover("v1", 1280, 520)));
        insertCatalog(MediaSupport.KIND_YINGYUE_BANNER, map("videoId", 4, "title", "星海归途", "subTitle", "跨星系回家的船票", "coverUrl", MediaSupport.cover("v4", 1280, 520)));
        insertCatalog(MediaSupport.KIND_YINGYUE_BANNER, map("videoId", 7, "title", "夏日放送", "subTitle", "综艺：把夏天过成电影", "coverUrl", MediaSupport.cover("v7", 1280, 520)));
    }

    private void insertCatalog(String kind, Map<String, Object> payload) {
        MediaCatalogEntity e = new MediaCatalogEntity();
        e.setKind(kind);
        e.setPayload(MediaSupport.writeJson(objectMapper, payload));
        e.setDeletedFlag(false);
        mediaCatalogDao.insert(e);
    }

    private Map<String, Object> map(Object... kv) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            map.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return map;
    }
}
