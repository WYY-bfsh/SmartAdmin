package net.lab1024.sa.admin.module.business.media.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.media.domain.form.MediaQueryForm;
import net.lab1024.sa.admin.module.business.media.service.MediaSupport;
import net.lab1024.sa.admin.module.business.media.service.MusicService;
import net.lab1024.sa.base.common.annoation.NoNeedLogin;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = AdminSwaggerTagConst.Business.MEDIA_MUSIC)
public class MusicController {

    @Resource
    private MusicService musicService;

    @Operation(summary = "发现页")
    @GetMapping("/media/music/discover")
    public ResponseDTO<Map<String, Object>> discover() {
        return ResponseDTO.ok(musicService.discover());
    }

    @Operation(summary = "歌曲列表")
    @GetMapping("/media/music/songs")
    public ResponseDTO<List<Map<String, Object>>> songs() {
        return ResponseDTO.ok(musicService.songs());
    }

    @Operation(summary = "歌单列表")
    @PostMapping("/media/music/playlist/query")
    public ResponseDTO<List<Map<String, Object>>> playlist(@RequestBody MediaQueryForm form) {
        return ResponseDTO.ok(musicService.playlists(form));
    }

    @Operation(summary = "歌单详情")
    @GetMapping("/media/music/playlist/detail/{id}")
    public ResponseDTO<Map<String, Object>> playlistDetail(@PathVariable Long id) {
        return ResponseDTO.ok(musicService.playlistDetail(id));
    }

    @Operation(summary = "歌手列表")
    @PostMapping("/media/music/artist/query")
    public ResponseDTO<List<Map<String, Object>>> artist(@RequestBody MediaQueryForm form) {
        return ResponseDTO.ok(musicService.artists());
    }

    @Operation(summary = "歌手详情")
    @GetMapping("/media/music/artist/detail/{id}")
    public ResponseDTO<Map<String, Object>> artistDetail(@PathVariable Long id) {
        return ResponseDTO.ok(musicService.artistDetail(id));
    }

    @Operation(summary = "专辑列表")
    @PostMapping("/media/music/album/query")
    public ResponseDTO<List<Map<String, Object>>> album(@RequestBody MediaQueryForm form) {
        return ResponseDTO.ok(musicService.albums());
    }

    @Operation(summary = "专辑详情")
    @GetMapping("/media/music/album/detail/{id}")
    public ResponseDTO<Map<String, Object>> albumDetail(@PathVariable Long id) {
        return ResponseDTO.ok(musicService.albumDetail(id));
    }

    @Operation(summary = "排行榜")
    @GetMapping("/media/music/rank/{type}")
    public ResponseDTO<List<Map<String, Object>>> rank(@PathVariable Integer type) {
        return ResponseDTO.ok(musicService.songs());
    }

    @Operation(summary = "电台")
    @GetMapping("/media/music/radio")
    public ResponseDTO<List<Map<String, Object>>> radio() {
        return ResponseDTO.ok(musicService.radios());
    }

    @Operation(summary = "搜索")
    @GetMapping("/media/music/search")
    public ResponseDTO<List<Map<String, Object>>> search(@RequestParam(required = false) String keyword) {
        return ResponseDTO.ok(musicService.search(keyword));
    }

    @Operation(summary = "我喜欢")
    @GetMapping("/media/music/favorite")
    public ResponseDTO<List<Map<String, Object>>> favorite() {
        return ResponseDTO.ok(musicService.favorite());
    }

    @Operation(summary = "最近播放")
    @GetMapping("/media/music/recent")
    public ResponseDTO<List<Map<String, Object>>> recent() {
        return ResponseDTO.ok(musicService.recent());
    }

    @Operation(summary = "喜欢/取消")
    @PostMapping("/media/music/like")
    public ResponseDTO<Boolean> like(@RequestBody Map<String, Object> body) {
        return ResponseDTO.ok(musicService.toggleLike(MediaSupport.toLong(body.get("songId"))));
    }

    @Operation(summary = "上报播放")
    @PostMapping("/media/music/play/report")
    public ResponseDTO<String> reportPlay(@RequestBody Map<String, Object> body) {
        musicService.reportPlay(MediaSupport.toLong(body.get("songId")));
        return ResponseDTO.ok();
    }

    @Operation(summary = "音频流（后端代拉，避免网易云防盗链）")
    @NoNeedLogin
    @GetMapping("/media/music/stream/{songId}")
    public void stream(@PathVariable Long songId, jakarta.servlet.http.HttpServletResponse response) {
        musicService.stream(songId, response);
    }

    @Operation(summary = "歌词")
    @GetMapping("/media/music/lyric/{songId}")
    public ResponseDTO<List<Map<String, Object>>> lyric(@PathVariable Long songId) {
        return ResponseDTO.ok(musicService.lyric(songId));
    }
}
