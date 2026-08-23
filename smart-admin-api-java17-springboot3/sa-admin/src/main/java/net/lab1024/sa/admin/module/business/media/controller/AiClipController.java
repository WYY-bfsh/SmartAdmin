package net.lab1024.sa.admin.module.business.media.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.media.domain.form.MediaQueryForm;
import net.lab1024.sa.admin.module.business.media.service.AiClipService;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = AdminSwaggerTagConst.Business.MEDIA_AI_CLIP)
public class AiClipController {

    @Resource
    private AiClipService aiClipService;

    @Operation(summary = "工作台")
    @GetMapping("/media/ai-clip/workspace")
    public ResponseDTO<Map<String, Object>> workspace() {
        return ResponseDTO.ok(aiClipService.workspace());
    }

    @Operation(summary = "作品列表")
    @PostMapping("/media/ai-clip/project/query")
    public ResponseDTO<List<Map<String, Object>>> project(@RequestBody MediaQueryForm form) {
        return ResponseDTO.ok(aiClipService.projects(form));
    }

    @Operation(summary = "作品详情")
    @GetMapping("/media/ai-clip/project/detail/{id}")
    public ResponseDTO<Map<String, Object>> projectDetail(@PathVariable Long id) {
        return ResponseDTO.ok(aiClipService.projectDetail(id));
    }

    @Operation(summary = "保存作品")
    @PostMapping("/media/ai-clip/project/save")
    public ResponseDTO<Long> saveProject(@RequestBody Map<String, Object> body) {
        return ResponseDTO.ok(aiClipService.saveProject(body));
    }

    @Operation(summary = "素材列表")
    @PostMapping("/media/ai-clip/material/query")
    public ResponseDTO<List<Map<String, Object>>> material(@RequestBody MediaQueryForm form) {
        return ResponseDTO.ok(aiClipService.materials(form));
    }

    @Operation(summary = "上传素材")
    @PostMapping("/media/ai-clip/material/upload")
    public ResponseDTO<Map<String, Object>> uploadMaterial(@RequestParam MultipartFile file,
                                                           @RequestParam(required = false) Integer type) {
        return aiClipService.uploadMaterial(file, type);
    }

    @Operation(summary = "模板列表")
    @PostMapping("/media/ai-clip/template/query")
    public ResponseDTO<List<Map<String, Object>>> template(@RequestBody MediaQueryForm form) {
        return ResponseDTO.ok(aiClipService.templates(form));
    }

    @Operation(summary = "模板详情")
    @GetMapping("/media/ai-clip/template/detail/{id}")
    public ResponseDTO<Map<String, Object>> templateDetail(@PathVariable Long id) {
        return ResponseDTO.ok(aiClipService.templateDetail(id));
    }

    @Operation(summary = "脚本列表")
    @PostMapping("/media/ai-clip/script/query")
    public ResponseDTO<List<Map<String, Object>>> script(@RequestBody MediaQueryForm form) {
        return ResponseDTO.ok(aiClipService.scripts(form));
    }

    @Operation(summary = "保存脚本")
    @PostMapping("/media/ai-clip/script/save")
    public ResponseDTO<Long> saveScript(@RequestBody Map<String, Object> body) {
        return ResponseDTO.ok(aiClipService.saveScript(body));
    }

    @Operation(summary = "成片任务")
    @PostMapping("/media/ai-clip/task/query")
    public ResponseDTO<List<Map<String, Object>>> task(@RequestBody MediaQueryForm form) {
        return ResponseDTO.ok(aiClipService.tasks(form));
    }

    @Operation(summary = "提交成片")
    @PostMapping("/media/ai-clip/task/create")
    public ResponseDTO<Long> createTask(@RequestBody Map<String, Object> body) {
        return ResponseDTO.ok(aiClipService.createTask(body));
    }
}
