package com.campushub.campususerservice.service;

import com.campushub.campususerservice.domain.dto.AdminUserQueryDTO;
import com.campushub.campususerservice.domain.dto.UserDTO;
import com.campushub.campususerservice.domain.dto.UserStatusUpdateDTO;
import com.campushub.campususerservice.domain.vo.UserBriefVO;
import com.campushub.campususerservice.domain.vo.UserPublicVO;
import com.campushub.campususerservice.domain.vo.UserStatsVO;
import com.campushub.campususerservice.domain.vo.UserVO;
import com.campushub.common.response.PageResult;

import java.util.List;

/**
 * 用户服务接口
 *
 * 定义用户相关的业务方法，具体实现见 UserServiceImpl。
 *
 * @author CampusHub
 */
public interface UserService {

    /**
     * 根据用户 ID 查询用户资料
     *
     * @param userId 用户 ID
     * @return 用户信息（不含密码）
     */
    UserVO getUserById(Long userId);

    /**
     * 查询指定用户的公开信息（供商品详情页展示"卖家介绍"/联系方式）
     *
     * 与 getUserById 的区别：只返回他人可见的字段，不暴露 username / role / status。
     *
     * 联系方式（手机号）只做登录门控：未登录时 phone 为空串、phoneVisible=false，
     * 已登录则完整明文返回（注册时手机号必填，因此登录用户之间互相可见是既定口径）。
     *
     * @param targetUserId  目标用户 ID
     * @param currentUserId 当前登录用户 ID（未登录为 null）
     * @return 用户公开信息（联系方式按登录态裁剪）
     */
    UserPublicVO getPublicUserById(Long targetUserId, Long currentUserId);

    /**
     * 根据用户 ID 更新用户资料
     * @param userDTO 用户资料
     * @return 更新后的用户信息
     */
    UserVO updateUser(UserDTO userDTO);

    /**
     * 批量查询用户简要信息（供其它服务跨服务调用，填充卖家昵称/头像等展示字段）
     *
     * @param ids 用户ID列表
     * @return 用户简要信息列表；ids 为空时返回空列表
     */
    List<UserBriefVO> listBriefByIds(List<Long> ids);

    /**
     * 后台分页查询用户（供 admin-service 调用）
     *
     * 关键词对用户名/昵称/手机号做模糊匹配，按注册时间倒序。
     *
     * @param query 分页 + 关键词参数
     * @return 用户分页结果（不含密码等敏感字段）
     */
    PageResult<UserVO> pageUsersForAdmin(AdminUserQueryDTO query);

    /**
     * 后台更新用户状态（启用/禁用）
     *
     * 不允许操作管理员账号；状态取值必须是 ACTIVE / DISABLED。
     *
     * @param id  目标用户ID
     * @param dto 状态更新参数
     * @return 更新后的用户信息
     */
    UserVO updateUserStatusByAdmin(Long id, UserStatusUpdateDTO dto);

    /**
     * 后台删除用户
     *
     * 不允许删除管理员账号；用户不存在抛 404。
     *
     * @param id 目标用户ID
     */
    void deleteUserByAdmin(Long id);

    /**
     * 后台用户统计（供 admin-service 的 Dashboard 聚合）
     *
     * 用户总数、今日新增、近 14 天注册趋势（日期轴连续，无新增的日期补 0）。
     *
     * @return 用户统计
     */
    UserStatsVO getUserStatsForAdmin();

}