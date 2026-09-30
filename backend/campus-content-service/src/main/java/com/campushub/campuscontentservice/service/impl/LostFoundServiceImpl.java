package com.campushub.campuscontentservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campushub.campuscontentservice.converter.LostFoundConverter;
import com.campushub.campuscontentservice.domain.dto.LostFoundPublishDTO;
import com.campushub.campuscontentservice.domain.dto.LostFoundQueryDTO;
import com.campushub.campuscontentservice.domain.dto.LostFoundStatusUpdateDTO;
import com.campushub.campuscontentservice.domain.entity.LostFound;
import com.campushub.campuscontentservice.domain.vo.ContentStatsVO;
import com.campushub.campuscontentservice.domain.vo.LostFoundStatItemVO;
import com.campushub.campuscontentservice.domain.vo.LostFoundVO;
import com.campushub.campuscontentservice.mapper.LostFoundMapper;
import com.campushub.campuscontentservice.service.LostFoundService;
import com.campushub.common.enums.ResultCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.response.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 失物招领服务实现类
 *
 * @author CampusHub
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LostFoundServiceImpl implements LostFoundService {

    /** 新发布记录的默认状态：进行中 */
    private static final String STATUS_OPEN = "OPEN";

    /** 已解决状态：失物找回 / 招领被认领后由发布者标记 */
    private static final String STATUS_RESOLVED = "RESOLVED";

    /** 类型：失物启事 */
    private static final String TYPE_LOST = "LOST";

    /** 类型：招领启事 */
    private static final String TYPE_FOUND = "FOUND";

    private final LostFoundMapper lostFoundMapper;
    private final LostFoundConverter lostFoundConverter;

    /**
     * {@inheritDoc}
     * @param query 分页 + 筛选参数
     * @return
     */
    @Override
    public PageResult<LostFoundVO> getLostFoundList(LostFoundQueryDTO query) {
        LambdaQueryWrapper<LostFound> wrapper = buildFilterWrapper(query)
                // 按发布时间倒序：最新发布的排前面
                .orderByDesc(LostFound::getCreatedAt);

        // 公共列表不下发联系方式（自己的"我的发布"列表才保留，见 getMyLostFoundList）
        return stripContact(getLostFoundVOPageResult(query, wrapper));
    }

    @Override
    public PageResult<LostFoundVO> getMyLostFoundList(Long publisherId, LostFoundQueryDTO query) {
        // 在公共筛选条件上追加"发布者=当前用户"：只查自己发布的，杜绝越权查他人
        LambdaQueryWrapper<LostFound> wrapper = buildFilterWrapper(query)
                .eq(LostFound::getPublisherId, publisherId)
                .orderByDesc(LostFound::getCreatedAt);

        return getLostFoundVOPageResult(query, wrapper);
    }

    /**
     * {@inheritDoc}
     * @param id             记录ID
     * @param contactVisible 当前请求者是否已登录
     * @return
     */
    @Override
    public LostFoundVO getLostFoundDetail(Long id, boolean contactVisible) {
        LostFound item = lostFoundMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "记录不存在或已删除");
        }
        // 详情只有一条，单独查一次发布者名片填充
        LostFoundVO vo = lostFoundConverter.toVO(item);

        // 联系方式门控：未登录不下发 contact（发布者填的是自由文本，前端无法脱敏，
        // 只能在服务端拦截）。前端拿到空 contact + contactVisible=false 后引导登录。
        vo.setContactVisible(contactVisible);
        if (!contactVisible) {
            vo.setContact("");
        }
        return vo;
    }

    /**
     * {@inheritDoc}
     * @param publisherId 发布者ID
     * @param dto 发布参数
     * @return
     */
    @Override
    public LostFoundVO publishLostFound(Long publisherId, LostFoundPublishDTO dto) {
        // 1.DTO → 实体：同名字段拷贝；images 类型不同(List→JSON字符串)排除后单独序列化
        LostFound item = new LostFound();
        BeanUtils.copyProperties(dto, item, "images");
        item.setImages(lostFoundConverter.toImagesJson(dto.getImages()));

        // 2.受控字段由后端填充，绝不信任前端：发布者=登录用户，状态=进行中
        item.setPublisherId(publisherId);
        item.setStatus(STATUS_OPEN);

        // 3.入库：id 自增、created_at/updated_at 由数据库默认值填充
        lostFoundMapper.insert(item);

        // 4.回查拿到数据库填充后的完整记录（id/时间），再转 VO 返回给前端跳详情用
        LostFound saved = lostFoundMapper.selectById(item.getId());
        return lostFoundConverter.toVO(saved);
    }

    /**
     * {@inheritDoc}
     * @param id 记录ID
     * @param userId 操作者ID
     * @param dto 状态更新参数
     * @return
     */
    @Override
    public LostFoundVO updateLostFoundStatus(Long id, Long userId, LostFoundStatusUpdateDTO dto) {
        // 1.查记录：不存在 → 404
        LostFound item = lostFoundMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "记录不存在或已删除");
        }

        // 2.归属校验：只有发布者本人能改状态，否则 → 403（防止越权改他人发布）
        if (!item.getPublisherId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能修改自己发布的内容");
        }

        // 3.状态值白名单校验：只接受 OPEN / RESOLVED，其余 → 400（防止脏数据进库）
        String status = dto.getStatus();
        if (!STATUS_OPEN.equals(status) && !STATUS_RESOLVED.equals(status)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "状态值不合法");
        }

        // 4.只更新 status 一列，内容/时间不碰
        lostFoundMapper.update(null,
                new LambdaUpdateWrapper<LostFound>()
                        .set(LostFound::getStatus, status)
                        .eq(LostFound::getId, id));

        // 5.回查最新记录再转 VO 返回（前端就地刷新状态标签）
        return lostFoundConverter.toVO(lostFoundMapper.selectById(id));
    }

    /**
     * {@inheritDoc}
     * @param id 记录ID
     * @param userId 操作者ID
     */
    @Override
    public void deleteLostFound(Long id, Long userId) {
        // 1.查记录：不存在 → 404
        LostFound item = lostFoundMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "记录不存在或已删除");
        }

        // 2.归属校验：只有发布者本人能删，否则 → 403（防止越权删他人发布）
        if (!item.getPublisherId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能删除自己发布的内容");
        }

        // 3.删除（无外键关联，直接删）
        lostFoundMapper.deleteById(id);
    }

    /**
     * 分页 + 筛选 + 转 VO + 组装 PageResult
     * @param query 分页 + 筛选参数
     * @param wrapper 筛选条件
     * @return 分页结果
     */
    @NonNull
    private PageResult<LostFoundVO> getLostFoundVOPageResult(LostFoundQueryDTO query, LambdaQueryWrapper<LostFound> wrapper) {
        Page<LostFound> page = lostFoundMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        List<LostFound> records = page.getRecords();
        if (records.isEmpty()) {
            return PageResult.of(List.of(), page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
        }

        List<LostFoundVO> vos = lostFoundConverter.toVOList(records);
        return PageResult.of(vos, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    /**
     * 裁剪一页结果里的联系方式（列表接口用）。
     *
     * 列表页不展示联系方式，没必要把发布者的电话塞进批量响应里被随意抓取；
     * 需要联系时走详情接口（那里按登录态做门控）。
     *
     * @param page 分页结果
     * @return 联系方式已置空的分页结果（records 为同一个列表对象，就地修改）
     */
    private PageResult<LostFoundVO> stripContact(PageResult<LostFoundVO> page) {
        page.getRecords().forEach(vo -> {
            vo.setContact("");
            vo.setContactVisible(false);
        });
        return page;
    }

    /**
     * 公共筛选条件：关键词(标题/描述 OR) + 类型，"有值才生效"。
     * 列表与"我的"列表共用；排序由调用方在末尾追加（保证 ORDER BY 在 WHERE 之后）
     */
    private LambdaQueryWrapper<LostFound> buildFilterWrapper(LostFoundQueryDTO query) {
        // 关键词用 and(...or...) 包住，避免 OR 泄漏破坏 type 的 AND 条件（同 announcement/activity/market 列表）
        return new LambdaQueryWrapper<LostFound>()
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(LostFound::getTitle, query.getKeyword())
                        .or()
                        .like(LostFound::getDescription, query.getKeyword()))
                .eq(StringUtils.hasText(query.getType()), LostFound::getType, query.getType());
    }

    /**
     * {@inheritDoc}
     * @param id  记录ID
     * @param dto 状态更新参数
     * @return
     */
    @Override
    public LostFoundVO updateLostFoundStatusByAdmin(Long id, LostFoundStatusUpdateDTO dto) {
        // 1.查记录：不存在 → 404
        LostFound item = lostFoundMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "记录不存在或已删除");
        }

        // 2.状态值白名单校验：只接受 OPEN / RESOLVED（后台无归属校验，管理员可处理任意人的记录）
        String status = dto.getStatus();
        if (!STATUS_OPEN.equals(status) && !STATUS_RESOLVED.equals(status)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "状态值不合法");
        }

        // 3.只更新 status 一列，内容/时间不碰
        lostFoundMapper.update(null,
                new LambdaUpdateWrapper<LostFound>()
                        .set(LostFound::getStatus, status)
                        .eq(LostFound::getId, id));

        // 4.回查最新记录再转 VO 返回（前端就地刷新状态标签）
        return lostFoundConverter.toVO(lostFoundMapper.selectById(id));
    }

    /**
     * {@inheritDoc}
     * @param id 记录ID
     */
    @Override
    public void deleteLostFoundByAdmin(Long id) {
        // 1.查记录：不存在 → 404
        LostFound item = lostFoundMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "记录不存在或已删除");
        }

        // 2.删除（无归属校验，管理员可删任意人的记录；无外键关联，直接删）
        lostFoundMapper.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContentStatsVO getContentStats() {
        // 总数 + 失物/招领各一条计数：三轻量 COUNT，没有趋势图需求就不搞 GROUP BY
        ContentStatsVO vo = new ContentStatsVO();
        vo.setLostFoundTotal(lostFoundMapper.selectCount(null));
        vo.setLostFoundStats(List.of(
                new LostFoundStatItemVO(TYPE_LOST, lostFoundMapper.selectCount(
                        new LambdaQueryWrapper<LostFound>().eq(LostFound::getType, TYPE_LOST))),
                new LostFoundStatItemVO(TYPE_FOUND, lostFoundMapper.selectCount(
                        new LambdaQueryWrapper<LostFound>().eq(LostFound::getType, TYPE_FOUND)))));
        return vo;
    }
}
