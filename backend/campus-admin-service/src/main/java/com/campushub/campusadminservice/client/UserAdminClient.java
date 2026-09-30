package com.campushub.campusadminservice.client;

import com.campushub.campusadminservice.domain.dto.UserStatusUpdateDTO;
import com.campushub.campusadminservice.domain.vo.UserStatsVO;
import com.campushub.campusadminservice.domain.vo.UserVO;
import com.campushub.common.response.PageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户管理 Feign 客户端
 *
 * 调用 user-service 的 /internal/admin/users 内部接口，把用户管理能力"借"给后台。
 *
 * 刻意不设 fallback：后台管理场景要"响亮地失败"——user-service 不可用时让异常冒泡，
 * 由全局处理器返回错误，前端提示"加载失败/操作失败"。若像 C 端那样降级返回空/静默成功，
 * 会让管理员误判（列表空以为没用户、封禁"成功"其实没生效），比直接报错更危险。
 *
 * @author CampusHub
 */
@FeignClient(name = "campus-user-service", path = "/internal/admin/users")
public interface UserAdminClient {

    /**
     * 分页查询用户
     *
     * @param keyword  关键词（用户名/昵称/手机号），null 时不筛选
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 用户分页结果
     */
    @GetMapping
    PageResult<UserVO> pageUsers(@RequestParam("keyword") String keyword,
                                 @RequestParam("pageNum") long pageNum,
                                 @RequestParam("pageSize") long pageSize);

    /**
     * 更新用户状态（启用/禁用）
     *
     * @param id  目标用户ID
     * @param dto 状态更新参数
     * @return 更新后的用户信息
     */
    @PutMapping("/{id}/status")
    UserVO updateStatus(@PathVariable("id") Long id, @RequestBody UserStatusUpdateDTO dto);

    /**
     * 删除用户
     *
     * @param id 目标用户ID
     */
    @DeleteMapping("/{id}")
    void delete(@PathVariable("id") Long id);

    /**
     * 用户统计：总数 + 今日新增 + 近 14 天注册趋势（供 Dashboard 聚合）
     *
     * @return 用户统计
     */
    @GetMapping("/stats")
    UserStatsVO stats();
}
