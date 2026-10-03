package net.lab1024.sa.admin.module.business.mall.service;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import net.lab1024.sa.admin.module.business.mall.constant.CommissionStatusEnum;
import net.lab1024.sa.admin.module.business.mall.dao.MallAddressDao;
import net.lab1024.sa.admin.module.business.mall.dao.MallCommissionDao;
import net.lab1024.sa.admin.module.business.mall.dao.MallMemberDao;
import net.lab1024.sa.admin.module.business.mall.domain.entity.MallAddressEntity;
import net.lab1024.sa.admin.module.business.mall.domain.entity.MallCommissionEntity;
import net.lab1024.sa.admin.module.business.mall.domain.entity.MallMemberEntity;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallAddressForm;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallLoginForm;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallMemberQueryForm;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallMemberUpdateForm;
import net.lab1024.sa.admin.module.business.mall.domain.form.MallRegisterForm;
import net.lab1024.sa.admin.module.business.mall.domain.vo.MallAddressVO;
import net.lab1024.sa.admin.module.business.mall.domain.vo.MallMemberVO;
import net.lab1024.sa.base.common.code.UserErrorCode;
import net.lab1024.sa.base.common.constant.RequestHeaderConst;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import net.lab1024.sa.base.module.support.file.constant.FileFolderTypeEnum;
import net.lab1024.sa.base.module.support.file.domain.vo.FileUploadVO;
import net.lab1024.sa.base.module.support.file.service.FileService;
import net.lab1024.sa.base.module.support.redis.RedisService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MallMemberService {

    private static final String TOKEN_PREFIX = "mall:token:";
    private static final long TOKEN_SECONDS = 7 * 24 * 3600L;
    private static final Map<String, Long> TOKEN_FALLBACK = new ConcurrentHashMap<>();

    @Resource
    private MallMemberDao mallMemberDao;

    @Resource
    private MallAddressDao mallAddressDao;

    @Resource
    private MallCommissionDao mallCommissionDao;

    @Resource
    private RedisService redisService;

    @Resource
    private FileService fileService;

    public void ensureReady() {
    }

    public ResponseDTO<FileUploadVO> uploadAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseDTO.userErrorParam("请选择图片");
        }
        String name = StringUtils.defaultString(file.getOriginalFilename()).toLowerCase();
        String contentType = StringUtils.defaultString(file.getContentType()).toLowerCase();
        boolean imageName = name.matches(".*\\.(png|jpe?g|gif|webp|bmp)$");
        // H5 选图经常没有扩展名，或 content-type 为 octet-stream
        if (!imageName) {
            String ext = "jpg";
            if (contentType.contains("png")) {
                ext = "png";
            } else if (contentType.contains("gif")) {
                ext = "gif";
            } else if (contentType.contains("webp")) {
                ext = "webp";
            }
            file = withFileName(file, "image." + ext);
        }
        return fileService.fileUpload(file, FileFolderTypeEnum.MEDIA.getValue(), null);
    }

    private static MultipartFile withFileName(MultipartFile file, String filename) {
        return new MultipartFile() {
            @Override
            public String getName() {
                return file.getName();
            }

            @Override
            public String getOriginalFilename() {
                return filename;
            }

            @Override
            public String getContentType() {
                return file.getContentType();
            }

            @Override
            public boolean isEmpty() {
                return file.isEmpty();
            }

            @Override
            public long getSize() {
                return file.getSize();
            }

            @Override
            public byte[] getBytes() throws IOException {
                return file.getBytes();
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return file.getInputStream();
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                file.transferTo(dest);
            }
        };
    }

    /**
     * 手机号注册。邀请码必填，绑定上级会员；密码 MD5 存储。
     */
    public ResponseDTO<MallMemberVO> register(MallRegisterForm form) {
        ensureReady();
        if (mallMemberDao.selectByPhone(form.getPhone()) != null) {
            return ResponseDTO.userErrorParam("该手机号已注册");
        }
        MallMemberEntity parent = mallMemberDao.selectByInviteCode(form.getInviteCode().trim().toUpperCase());
        if (parent == null) {
            return ResponseDTO.userErrorParam("邀请码无效");
        }
        MallMemberEntity entity = new MallMemberEntity();
        entity.setPhone(form.getPhone());
        entity.setNickname(StringUtils.defaultIfBlank(form.getNickname(), "用户" + form.getPhone().substring(7)));
        entity.setPassword(DigestUtil.md5Hex(form.getPassword()));
        entity.setAvatar(StringUtils.trimToNull(form.getAvatar()));
        entity.setWechatPayQr(StringUtils.trimToNull(form.getWechatPayQr()));
        entity.setWechatReceiveQr(StringUtils.trimToNull(form.getWechatReceiveQr()));
        entity.setInviteCode(nextInviteCode());
        entity.setParentMemberId(parent.getMemberId());
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateTime(LocalDateTime.now());
        mallMemberDao.insert(entity);
        return ResponseDTO.ok(toLoginVo(entity));
    }

    public ResponseDTO<MallMemberVO> login(MallLoginForm form) {
        ensureReady();
        MallMemberEntity entity = mallMemberDao.selectByPhone(form.getPhone());
        if (entity == null || !DigestUtil.md5Hex(form.getPassword()).equals(entity.getPassword())) {
            return ResponseDTO.userErrorParam("手机号或密码错误");
        }
        return ResponseDTO.ok(toLoginVo(entity));
    }

    public ResponseDTO<MallMemberVO> me() {
        MallMemberEntity entity = requireMember();
        MallMemberVO vo = SmartBeanUtil.copy(entity, MallMemberVO.class);
        fillCommission(vo);
        if (entity.getParentMemberId() != null) {
            MallMemberEntity parent = mallMemberDao.selectById(entity.getParentMemberId());
            if (parent != null) {
                vo.setParentPhone(parent.getPhone());
            }
        }
        Long team = mallMemberDao.selectCount(new LambdaQueryWrapper<MallMemberEntity>()
                .eq(MallMemberEntity::getParentMemberId, entity.getMemberId())
                .eq(MallMemberEntity::getDeletedFlag, false));
        vo.setTeamCount(team == null ? 0 : team.intValue());
        return ResponseDTO.ok(vo);
    }

    public ResponseDTO<PageResult<MallMemberVO>> queryMembers(MallMemberQueryForm queryForm) {
        ensureReady();
        Page<MallMemberEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        LambdaQueryWrapper<MallMemberEntity> wrapper = new LambdaQueryWrapper<MallMemberEntity>()
                .eq(MallMemberEntity::getDeletedFlag, false)
                .like(StringUtils.isNotBlank(queryForm.getPhone()), MallMemberEntity::getPhone, queryForm.getPhone())
                .like(StringUtils.isNotBlank(queryForm.getInviteCode()), MallMemberEntity::getInviteCode, queryForm.getInviteCode())
                .orderByDesc(MallMemberEntity::getMemberId);
        mallMemberDao.selectPage(page, wrapper);
        List<MallMemberVO> list = SmartBeanUtil.copyList(page.getRecords(), MallMemberVO.class);
        list.forEach(this::fillCommission);
        return ResponseDTO.ok(SmartPageUtil.convert2PageResult(page, list));
    }

    public ResponseDTO<String> updateMember(MallMemberUpdateForm form) {
        ensureReady();
        MallMemberEntity entity = mallMemberDao.selectById(form.getMemberId());
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) {
            return ResponseDTO.userErrorParam("会员不存在");
        }
        LambdaUpdateWrapper<MallMemberEntity> wrapper = new LambdaUpdateWrapper<MallMemberEntity>()
                .eq(MallMemberEntity::getMemberId, entity.getMemberId());
        if (form.getNickname() != null) {
            wrapper.set(MallMemberEntity::getNickname, StringUtils.trimToNull(form.getNickname()));
        }
        if (form.getAvatar() != null) {
            wrapper.set(MallMemberEntity::getAvatar, StringUtils.trimToNull(form.getAvatar()));
        }
        if (form.getWechatPayQr() != null) {
            wrapper.set(MallMemberEntity::getWechatPayQr, StringUtils.trimToNull(form.getWechatPayQr()));
        }
        if (form.getWechatReceiveQr() != null) {
            wrapper.set(MallMemberEntity::getWechatReceiveQr, StringUtils.trimToNull(form.getWechatReceiveQr()));
        }
        mallMemberDao.update(null, wrapper);
        return ResponseDTO.ok();
    }

    public ResponseDTO<List<MallAddressVO>> listAddress() {
        MallMemberEntity member = requireMember();
        List<MallAddressEntity> list = mallAddressDao.selectList(new LambdaQueryWrapper<MallAddressEntity>()
                .eq(MallAddressEntity::getMemberId, member.getMemberId())
                .eq(MallAddressEntity::getDeletedFlag, false)
                .orderByDesc(MallAddressEntity::getDefaultFlag)
                .orderByDesc(MallAddressEntity::getAddressId));
        return ResponseDTO.ok(SmartBeanUtil.copyList(list, MallAddressVO.class));
    }

    public ResponseDTO<String> saveAddress(MallAddressForm form) {
        MallMemberEntity member = requireMember();
        if (Boolean.TRUE.equals(form.getDefaultFlag())) {
            List<MallAddressEntity> old = mallAddressDao.selectList(new LambdaQueryWrapper<MallAddressEntity>()
                    .eq(MallAddressEntity::getMemberId, member.getMemberId())
                    .eq(MallAddressEntity::getDeletedFlag, false));
            for (MallAddressEntity item : old) {
                if (Boolean.TRUE.equals(item.getDefaultFlag())) {
                    item.setDefaultFlag(Boolean.FALSE);
                    mallAddressDao.updateById(item);
                }
            }
        }
        if (form.getAddressId() == null) {
            MallAddressEntity entity = SmartBeanUtil.copy(form, MallAddressEntity.class);
            entity.setMemberId(member.getMemberId());
            entity.setDeletedFlag(Boolean.FALSE);
            entity.setCreateTime(LocalDateTime.now());
            mallAddressDao.insert(entity);
        } else {
            MallAddressEntity entity = mallAddressDao.selectById(form.getAddressId());
            if (entity == null || !entity.getMemberId().equals(member.getMemberId())) {
                return ResponseDTO.userErrorParam("地址不存在");
            }
            entity.setReceiverName(form.getReceiverName());
            entity.setReceiverPhone(form.getReceiverPhone());
            entity.setProvince(form.getProvince());
            entity.setCity(form.getCity());
            entity.setDistrict(form.getDistrict());
            entity.setDetail(form.getDetail());
            entity.setDefaultFlag(form.getDefaultFlag());
            mallAddressDao.updateById(entity);
        }
        return ResponseDTO.ok();
    }

    public MallMemberEntity requireMember() {
        ensureReady();
        Long memberId = currentMemberId();
        if (memberId == null) {
            throw new MallLoginException();
        }
        MallMemberEntity entity = mallMemberDao.selectById(memberId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) {
            throw new MallLoginException();
        }
        return entity;
    }

    public Long currentMemberId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader(RequestHeaderConst.MALL_TOKEN);
        if (StringUtils.isBlank(token)) {
            return null;
        }
        Long fallbackId = TOKEN_FALLBACK.get(token);
        try {
            String value = redisService.get(TOKEN_PREFIX + token);
            if (StringUtils.isNotBlank(value)) {
                return Long.parseLong(value);
            }
        } catch (Exception ignored) {
            // Redis 不可用时走内存
        }
        return fallbackId;
    }

    public ResponseDTO<?> loginRequired(ResponseDTO<?> error) {
        return error;
    }

    private MallMemberVO toLoginVo(MallMemberEntity entity) {
        String token = UUID.randomUUID().toString().replace("-", "");
        TOKEN_FALLBACK.put(token, entity.getMemberId());
        try {
            redisService.set(TOKEN_PREFIX + token, String.valueOf(entity.getMemberId()), TOKEN_SECONDS);
        } catch (Exception ignored) {
            // Redis 不可用时使用内存 token
        }
        MallMemberVO vo = SmartBeanUtil.copy(entity, MallMemberVO.class);
        vo.setToken(token);
        fillCommission(vo);
        return vo;
    }

    private void fillCommission(MallMemberVO vo) {
        if (vo.getMemberId() == null) {
            return;
        }
        List<MallCommissionEntity> list = mallCommissionDao.selectList(new LambdaQueryWrapper<MallCommissionEntity>()
                .eq(MallCommissionEntity::getMemberId, vo.getMemberId()));
        BigDecimal frozen = BigDecimal.ZERO;
        BigDecimal settled = BigDecimal.ZERO;
        for (MallCommissionEntity item : list) {
            if (CommissionStatusEnum.FROZEN.equalsValue(item.getStatus())) {
                frozen = frozen.add(item.getAmount());
            } else if (CommissionStatusEnum.SETTLED.equalsValue(item.getStatus())) {
                settled = settled.add(item.getAmount());
            }
        }
        vo.setFrozenCommission(frozen);
        vo.setSettledCommission(settled);
    }

    private String nextInviteCode() {
        for (int i = 0; i < 8; i++) {
            String code = "SA" + RandomUtil.randomString("ABCDEFGHJKLMNPQRSTUVWXYZ23456789", 6);
            if (mallMemberDao.selectByInviteCode(code) == null) {
                return code;
            }
        }
        return "SA" + System.currentTimeMillis();
    }

    public static class MallLoginException extends RuntimeException {
        public MallLoginException() {
            super(UserErrorCode.LOGIN_STATE_INVALID.getMsg());
        }
    }
}
