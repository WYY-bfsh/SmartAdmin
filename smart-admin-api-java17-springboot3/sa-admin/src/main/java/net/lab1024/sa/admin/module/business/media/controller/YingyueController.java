package net.lab1024.sa.admin.module.business.media.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.media.domain.form.MediaQueryForm;
import net.lab1024.sa.admin.module.business.media.service.MediaSupport;
import net.lab1024.sa.admin.module.business.media.service.YingyueService;
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
@Tag(name = AdminSwaggerTagConst.Business.MEDIA_YINGYUE)
public class YingyueController {

    @Resource
    private YingyueService yingyueService;

    @Operation(summary = "首页")
    @GetMapping("/media/yingyue/home")
    public ResponseDTO<Map<String, Object>> home() {
        return ResponseDTO.ok(yingyueService.home());
    }

    @Operation(summary = "片库")
    @PostMapping("/media/yingyue/library/query")
    public ResponseDTO<List<Map<String, Object>>> library(@RequestBody MediaQueryForm form) {
        return ResponseDTO.ok(yingyueService.library(form));
    }

    @Operation(summary = "频道")
    @GetMapping("/media/yingyue/channel")
    public ResponseDTO<List<Map<String, Object>>> channel(@RequestParam(required = false) Integer category) {
        MediaQueryForm form = new MediaQueryForm();
        form.setCategory(category);
        return ResponseDTO.ok(yingyueService.library(form));
    }

    @Operation(summary = "排行")
    @GetMapping("/media/yingyue/rank")
    public ResponseDTO<List<Map<String, Object>>> rank() {
        return ResponseDTO.ok(yingyueService.rank());
    }

    @Operation(summary = "详情")
    @GetMapping("/media/yingyue/detail/{id}")
    public ResponseDTO<Map<String, Object>> detail(@PathVariable Long id) {
        return ResponseDTO.ok(yingyueService.detail(id));
    }

    @Operation(summary = "播放信息")
    @GetMapping("/media/yingyue/play/{id}")
    public ResponseDTO<Map<String, Object>> play(@PathVariable Long id, @RequestParam(required = false) Integer episodeNo) {
        return ResponseDTO.ok(yingyueService.detail(id));
    }

    @Operation(summary = "搜索")
    @GetMapping("/media/yingyue/search")
    public ResponseDTO<List<Map<String, Object>>> search(@RequestParam(required = false) String keyword) {
        return ResponseDTO.ok(yingyueService.search(keyword));
    }

    @Operation(summary = "片单")
    @GetMapping("/media/yingyue/favorite")
    public ResponseDTO<List<Map<String, Object>>> favorite() {
        return ResponseDTO.ok(yingyueService.favorite());
    }

    @Operation(summary = "观看历史")
    @GetMapping("/media/yingyue/history")
    public ResponseDTO<List<Map<String, Object>>> history() {
        return ResponseDTO.ok(yingyueService.history());
    }

    @Operation(summary = "加入/取消片单")
    @PostMapping("/media/yingyue/favorite/toggle")
    public ResponseDTO<Boolean> toggleFavorite(@RequestBody Map<String, Object> body) {
        return ResponseDTO.ok(yingyueService.toggleFavorite(MediaSupport.toLong(body.get("videoId"))));
    }

    @Operation(summary = "上报进度")
    @PostMapping("/media/yingyue/history/report")
    public ResponseDTO<String> report(@RequestBody Map<String, Object> body) {
        yingyueService.reportHistory(body);
        return ResponseDTO.ok();
    }

    @Operation(summary = "视频流（后端代拉，避免片源防盗链）")
    @NoNeedLogin
    @GetMapping("/media/yingyue/stream/{videoId}")
    public void stream(@PathVariable Long videoId, jakarta.servlet.http.HttpServletResponse response) {
        yingyueService.stream(videoId, response);
    }
}
