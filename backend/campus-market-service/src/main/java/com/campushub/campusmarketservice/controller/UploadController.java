package com.campushub.campusmarketservice.controller;

import com.campushub.campusmarketservice.domain.vo.UploadResultVO;
import com.campushub.common.enums.ResultCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.response.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 图片上传控制器
 * <p>
 * 挂在 /api/market/** 下，复用网关现有路由（无需改 Nacos 网关配置）。
 * 文件保存到本地磁盘，返回一个"直连 market 静态资源"的 URL：
 * 因为 <img> 标签不携带 JWT，图片若走网关 /api/** 会被鉴权拦截 401，故取图绕过网关。
 *
 * @author CampusHub
 */
@Slf4j
@RestController
@RequestMapping("/api/market")
public class UploadController {

    /**
     * 允许的图片扩展名
     */
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp");

    /**
     * 单张大小上限 5MB（与前端 ImageUpload 提示一致）
     */
    private static final long MAX_SIZE = 5L * 1024 * 1024;

    @Value("${campushub.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${campushub.upload.base-url:http://localhost:8084/uploads}")
    private String baseUrl;

    /**
     * 上传单张图片
     *
     * @param file multipart 文件（字段名 file）
     * @return 可访问的图片 URL
     */
    @PostMapping("/upload/image")
    public R<UploadResultVO> uploadImage(@RequestParam("file") MultipartFile file) {
        // 1.基础校验：非空、大小、类型
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请选择要上传的图片");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "图片大小不能超过 5MB");
        }
        String ext = resolveExtension(file.getOriginalFilename());
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅支持 JPG / PNG / WebP 图片");
        }

        // 2.用 UUID 重命名，避免同名覆盖与中文/空格文件名问题
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;

        // 3.保存到本地磁盘（目录不存在则创建）
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath();
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(filename));
        } catch (IOException e) {
            throw new BusinessException(ResultCode.SERVER_ERROR, "图片保存失败,请重试");
        }

        // 4.返回直连 market 静态资源的 URL
        return R.success(new UploadResultVO(baseUrl + "/" + filename));
    }

    /**
     * 提取小写扩展名；无扩展名返回空串
     */
    private String resolveExtension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dot = originalFilename.lastIndexOf('.');
        return dot < 0 ? "" : originalFilename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * 删除图片文件（用于表单移除缩略图时清理"本次会话上传"的临时文件）
     *
     * 注意：只应删除"尚未发布"的临时上传；已发布商品的图片不要调此接口，
     * 否则用户取消编辑时会误删正式图片。前端由 ImageUpload 的会话记录把关。
     *
     * @param filename 文件名（UUID 命名，不含路径）
     */
    @DeleteMapping("/uploads/{filename}")
    public R<Void> deleteImage(@PathVariable("filename") String filename) {
        // 防路径穿越：只允许纯文件名，拒绝 / \ .. 等，避免删除上传目录之外的文件
        if (filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "非法的文件名");
        }
        try {
            Path target = Paths.get(uploadDir).toAbsolutePath().resolve(filename);
            Files.deleteIfExists(target);
        } catch (IOException e) {
            // 删除失败不阻断前端流程，仅记录日志
            log.warn("删除上传文件失败: {}", filename, e);
        }
        return R.success(null);
    }
}