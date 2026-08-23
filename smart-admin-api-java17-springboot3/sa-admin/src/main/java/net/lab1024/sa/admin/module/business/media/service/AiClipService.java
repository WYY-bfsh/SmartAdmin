package net.lab1024.sa.admin.module.business.media.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.business.media.dao.AiMaterialDao;
import net.lab1024.sa.admin.module.business.media.dao.AiProjectDao;
import net.lab1024.sa.admin.module.business.media.dao.AiTaskDao;
import net.lab1024.sa.admin.module.business.media.dao.MediaCatalogDao;
import net.lab1024.sa.admin.module.business.media.domain.entity.AiMaterialEntity;
import net.lab1024.sa.admin.module.business.media.domain.entity.AiProjectEntity;
import net.lab1024.sa.admin.module.business.media.domain.entity.AiTaskEntity;
import net.lab1024.sa.admin.module.business.media.domain.entity.MediaCatalogEntity;
import net.lab1024.sa.admin.module.business.media.domain.form.MediaQueryForm;
import net.lab1024.sa.admin.util.AdminRequestUtil;
import net.lab1024.sa.base.common.domain.RequestUser;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.module.support.file.constant.FileFolderTypeEnum;
import net.lab1024.sa.base.module.support.file.domain.vo.FileUploadVO;
import net.lab1024.sa.base.module.support.file.service.FileService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AiClipService {

    @Resource
    private MediaSeedService mediaSeedService;
    @Resource
    private AiProjectDao aiProjectDao;
    @Resource
    private AiMaterialDao aiMaterialDao;
    @Resource
    private AiTaskDao aiTaskDao;
    @Resource
    private MediaCatalogDao mediaCatalogDao;
    @Resource
    private FileService fileService;
    @Resource
    private ObjectMapper objectMapper;

    public Map<String, Object> workspace() {
        mediaSeedService.ensureReady();
        List<Map<String, Object>> projects = projects(new MediaQueryForm());
        Map<String, Object> vo = new LinkedHashMap<>();
        vo.put("projectCount", projects.size());
        vo.put("materialCount", materials(new MediaQueryForm()).size());
        vo.put("templateCount", templates(new MediaQueryForm()).size());
        vo.put("taskToday", todayTaskCount());
        vo.put("recentProjects", projects.stream().limit(3).collect(Collectors.toList()));
        return vo;
    }

    public List<Map<String, Object>> projects(MediaQueryForm form) {
        mediaSeedService.ensureReady();
        LambdaQueryWrapper<AiProjectEntity> wrapper = new LambdaQueryWrapper<AiProjectEntity>()
                .eq(AiProjectEntity::getDeletedFlag, false)
                .eq(form != null && form.getStatus() != null, AiProjectEntity::getStatus, form == null ? null : form.getStatus())
                .like(form != null && StringUtils.isNotBlank(form.getKeyword()), AiProjectEntity::getTitle, form == null ? null : form.getKeyword())
                .orderByDesc(AiProjectEntity::getUpdateTime);
        return aiProjectDao.selectList(wrapper).stream().map(this::toProject).collect(Collectors.toList());
    }

    public Map<String, Object> projectDetail(Long id) {
        mediaSeedService.ensureReady();
        AiProjectEntity entity = aiProjectDao.selectById(id);
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) {
            List<Map<String, Object>> list = projects(new MediaQueryForm());
            return list.isEmpty() ? new LinkedHashMap<>() : list.get(0);
        }
        return toProject(entity);
    }

    public Long saveProject(Map<String, Object> body) {
        mediaSeedService.ensureReady();
        Long id = MediaSupport.toLong(body.get("projectId"));
        AiProjectEntity entity = id == null ? new AiProjectEntity() : aiProjectDao.selectById(id);
        if (entity == null) {
            entity = new AiProjectEntity();
        }
        entity.setTitle(String.valueOf(body.getOrDefault("title", "未命名作品")));
        entity.setScene(MediaSupport.toInt(body.getOrDefault("scene", 1)));
        entity.setStatus(MediaSupport.toInt(body.getOrDefault("status", 10)));
        entity.setDuration(String.valueOf(body.getOrDefault("duration", "00:00")));
        entity.setRatio(String.valueOf(body.getOrDefault("ratio", "9:16")));
        if (StringUtils.isBlank(entity.getCoverUrl())) {
            entity.setCoverUrl(MediaSupport.cover("draft" + System.currentTimeMillis()));
        }
        entity.setDeletedFlag(false);
        if (entity.getProjectId() == null) {
            aiProjectDao.insert(entity);
        } else {
            aiProjectDao.updateById(entity);
        }
        return entity.getProjectId();
    }

    public List<Map<String, Object>> materials(MediaQueryForm form) {
        mediaSeedService.ensureReady();
        LambdaQueryWrapper<AiMaterialEntity> wrapper = new LambdaQueryWrapper<AiMaterialEntity>()
                .eq(AiMaterialEntity::getDeletedFlag, false)
                .eq(form != null && form.getType() != null && form.getType() > 0, AiMaterialEntity::getType, form == null ? null : form.getType())
                .orderByDesc(AiMaterialEntity::getCreateTime);
        return aiMaterialDao.selectList(wrapper).stream().map(this::toMaterial).collect(Collectors.toList());
    }

    public ResponseDTO<Map<String, Object>> uploadMaterial(MultipartFile file, Integer type) {
        mediaSeedService.ensureReady();
        RequestUser user = AdminRequestUtil.getRequestUser();
        ResponseDTO<FileUploadVO> upload = fileService.fileUpload(file, FileFolderTypeEnum.MEDIA.getValue(), user);
        if (!upload.getOk()) {
            return ResponseDTO.error(upload);
        }
        FileUploadVO data = upload.getData();
        String original = file.getOriginalFilename();
        Integer materialType = type != null && type > 0 ? type : MediaSupport.guessMaterialType(original);
        AiMaterialEntity entity = new AiMaterialEntity();
        entity.setName(StringUtils.defaultIfBlank(original, data.getFileName()));
        entity.setType(materialType);
        entity.setDuration("-");
        entity.setSize(MediaSupport.formatSize(file.getSize()));
        entity.setFileKey(data.getFileKey());
        entity.setFileUrl(data.getFileUrl());
        entity.setCoverUrl(materialType == 2 ? data.getFileUrl() : MediaSupport.cover("up" + System.currentTimeMillis()));
        entity.setDeletedFlag(false);
        aiMaterialDao.insert(entity);
        return ResponseDTO.ok(toMaterial(entity));
    }

    public List<Map<String, Object>> templates(MediaQueryForm form) {
        mediaSeedService.ensureReady();
        Integer scene = form == null ? null : form.getScene();
        return catalog(MediaSupport.KIND_TEMPLATE).stream()
                .filter(e -> scene == null || scene == 0 || Objects.equals(MediaSupport.toInt(e.get("scene")), scene))
                .collect(Collectors.toList());
    }

    public Map<String, Object> templateDetail(Long id) {
        return templates(new MediaQueryForm()).stream()
                .filter(e -> id != null && id.equals(MediaSupport.toLong(e.get("templateId"))))
                .findFirst()
                .orElseGet(() -> {
                    List<Map<String, Object>> list = templates(new MediaQueryForm());
                    return list.isEmpty() ? new LinkedHashMap<>() : list.get(0);
                });
    }

    public List<Map<String, Object>> scripts(MediaQueryForm form) {
        mediaSeedService.ensureReady();
        String keyword = form == null ? null : form.getKeyword();
        return catalog(MediaSupport.KIND_SCRIPT).stream()
                .filter(e -> StringUtils.isBlank(keyword) || String.valueOf(e.get("title")).contains(keyword))
                .collect(Collectors.toList());
    }

    public Long saveScript(Map<String, Object> body) {
        mediaSeedService.ensureReady();
        Long scriptId = MediaSupport.toLong(body.get("scriptId"));
        String content = String.valueOf(body.getOrDefault("content", ""));
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("scriptId", scriptId == null ? System.currentTimeMillis() : scriptId);
        payload.put("title", body.getOrDefault("title", "未命名脚本"));
        payload.put("scene", body.getOrDefault("scene", 1));
        payload.put("wordCount", content.length());
        payload.put("content", content);
        payload.put("updateTime", MediaSupport.now());

        MediaCatalogEntity exist = findCatalogByField(MediaSupport.KIND_SCRIPT, "scriptId", scriptId);
        if (exist == null) {
            MediaCatalogEntity entity = new MediaCatalogEntity();
            entity.setKind(MediaSupport.KIND_SCRIPT);
            entity.setPayload(MediaSupport.writeJson(objectMapper, payload));
            entity.setDeletedFlag(false);
            mediaCatalogDao.insert(entity);
        } else {
            exist.setPayload(MediaSupport.writeJson(objectMapper, payload));
            mediaCatalogDao.updateById(exist);
        }
        return MediaSupport.toLong(payload.get("scriptId"));
    }

    public List<Map<String, Object>> tasks(MediaQueryForm form) {
        mediaSeedService.ensureReady();
        LambdaQueryWrapper<AiTaskEntity> wrapper = new LambdaQueryWrapper<AiTaskEntity>()
                .eq(AiTaskEntity::getDeletedFlag, false)
                .eq(form != null && form.getStatus() != null, AiTaskEntity::getStatus, form == null ? null : form.getStatus())
                .orderByDesc(AiTaskEntity::getCreateTime);
        return aiTaskDao.selectList(wrapper).stream().map(this::toTask).collect(Collectors.toList());
    }

    public Long createTask(Map<String, Object> body) {
        mediaSeedService.ensureReady();
        AiTaskEntity entity = new AiTaskEntity();
        entity.setTitle(String.valueOf(body.getOrDefault("title", "新的成片任务")));
        entity.setType(String.valueOf(body.getOrDefault("type", "一键成片")));
        entity.setProgress(12);
        entity.setStatus(20);
        entity.setDeletedFlag(false);
        aiTaskDao.insert(entity);
        return entity.getTaskId();
    }

    private long todayTaskCount() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        return aiTaskDao.selectCount(new LambdaQueryWrapper<AiTaskEntity>()
                .eq(AiTaskEntity::getDeletedFlag, false)
                .ge(AiTaskEntity::getCreateTime, start));
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

    private MediaCatalogEntity findCatalogByField(String kind, String field, Long id) {
        if (id == null) {
            return null;
        }
        return mediaCatalogDao.selectList(new LambdaQueryWrapper<MediaCatalogEntity>()
                        .eq(MediaCatalogEntity::getKind, kind)
                        .eq(MediaCatalogEntity::getDeletedFlag, false))
                .stream()
                .filter(e -> id.equals(MediaSupport.toLong(MediaSupport.readMap(objectMapper, e.getPayload()).get(field))))
                .findFirst()
                .orElse(null);
    }

    private Map<String, Object> toProject(AiProjectEntity e) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("projectId", e.getProjectId());
        map.put("title", e.getTitle());
        map.put("scene", e.getScene());
        map.put("status", e.getStatus());
        map.put("duration", e.getDuration());
        map.put("ratio", e.getRatio());
        map.put("coverUrl", e.getCoverUrl());
        map.put("updateTime", MediaSupport.formatTime(e.getUpdateTime()));
        return map;
    }

    private Map<String, Object> toMaterial(AiMaterialEntity e) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("materialId", e.getMaterialId());
        map.put("name", e.getName());
        map.put("type", e.getType());
        map.put("duration", e.getDuration());
        map.put("size", e.getSize());
        map.put("coverUrl", e.getCoverUrl());
        map.put("fileKey", e.getFileKey());
        map.put("fileUrl", e.getFileUrl());
        map.put("createTime", MediaSupport.formatTime(e.getCreateTime()));
        return map;
    }

    private Map<String, Object> toTask(AiTaskEntity e) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("taskId", e.getTaskId());
        map.put("title", e.getTitle());
        map.put("type", e.getType());
        map.put("progress", e.getProgress());
        map.put("status", e.getStatus());
        map.put("resultUrl", e.getResultUrl());
        map.put("createTime", MediaSupport.formatTime(e.getCreateTime()));
        return map;
    }
}
