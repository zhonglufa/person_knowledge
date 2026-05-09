package com.qst.lm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qst.lm.common.R;
import com.qst.lm.mapper.AnnouncementMapper;
import com.qst.lm.mapper.AnnouncementStatisticsMapper;
import com.qst.lm.pojo.Announcement;
import com.qst.lm.pojo.AnnouncementStatistics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/announcement")
@Tag(name = "公告模块", description = "用户侧公告展示相关接口")
public class AnnouncementController {

    @Resource
    private AnnouncementMapper announcementMapper;

    @Resource
    private AnnouncementStatisticsMapper announcementStatisticsMapper;

    @GetMapping("/active")
    @Operation(summary = "获取生效中的公告", description = "获取当前生效且未过期的已发布公告列表")
    public R<Map<String, Object>> getActiveAnnouncements(@RequestParam(required = false, defaultValue = "10") Integer size) {
        int pageSize = size == null || size < 1 ? 10 : Math.min(size, 50);

        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, 1)
                .eq(Announcement::getDeleted, 0)
                .and(w -> w.isNull(Announcement::getEffectiveAt).or().le(Announcement::getEffectiveAt, LocalDateTime.now()))
                .and(w -> w.isNull(Announcement::getExpireAt).or().gt(Announcement::getExpireAt, LocalDateTime.now()))
                .orderByDesc(Announcement::getPriority)
                .orderByDesc(Announcement::getEffectiveAt)
                .orderByDesc(Announcement::getCreatedAt)
                .last("limit " + pageSize);

        List<Announcement> list = announcementMapper.selectList(wrapper);

        Map<String, Object> result = new HashMap<>(2);
        result.put("list", list);
        result.put("total", list == null ? 0 : list.size());
        return R.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取公告详情", description = "获取公告详情，并增加浏览次数")
    public R<Map<String, Object>> getAnnouncementDetail(@PathVariable Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null || announcement.getDeleted() != null && announcement.getDeleted() == 1) {
            return R.error("公告不存在");
        }

        if (announcement.getStatus() == null || announcement.getStatus() != 1) {
            return R.error("公告未发布");
        }

        LocalDateTime now = LocalDateTime.now();
        if (announcement.getEffectiveAt() != null && announcement.getEffectiveAt().isAfter(now)) {
            return R.error("公告未生效");
        }
        if (announcement.getExpireAt() != null && !announcement.getExpireAt().isAfter(now)) {
            return R.error("公告已过期");
        }

        LambdaQueryWrapper<AnnouncementStatistics> statsWrapper = new LambdaQueryWrapper<>();
        statsWrapper.eq(AnnouncementStatistics::getAnnouncementId, id);
        AnnouncementStatistics stats = announcementStatisticsMapper.selectOne(statsWrapper);

        if (stats == null) {
            stats = new AnnouncementStatistics();
            stats.setAnnouncementId(id);
            stats.setViewCount(0);
            stats.setReadUserCount(0);
            announcementStatisticsMapper.insert(stats);
        }

        LambdaUpdateWrapper<AnnouncementStatistics> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AnnouncementStatistics::getAnnouncementId, id)
                .set(AnnouncementStatistics::getViewCount, (stats.getViewCount() == null ? 0 : stats.getViewCount()) + 1);
        announcementStatisticsMapper.update(null, updateWrapper);

        Map<String, Object> data = new HashMap<>(2);
        data.put("announcement", announcement);
        data.put("statistics", Map.of(
                "viewCount", (stats.getViewCount() == null ? 0 : stats.getViewCount()) + 1,
                "readUserCount", stats.getReadUserCount() == null ? 0 : stats.getReadUserCount()
        ));
        return R.success(data);
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "上报公告已读", description = "公告阅读上报，增加阅读用户数")
    public R<String> markAnnouncementRead(@PathVariable Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null || announcement.getDeleted() != null && announcement.getDeleted() == 1) {
            return R.error("公告不存在");
        }

        LambdaQueryWrapper<AnnouncementStatistics> statsWrapper = new LambdaQueryWrapper<>();
        statsWrapper.eq(AnnouncementStatistics::getAnnouncementId, id);
        AnnouncementStatistics stats = announcementStatisticsMapper.selectOne(statsWrapper);
        if (stats == null) {
            stats = new AnnouncementStatistics();
            stats.setAnnouncementId(id);
            stats.setViewCount(0);
            stats.setReadUserCount(0);
            announcementStatisticsMapper.insert(stats);
        }

        LambdaUpdateWrapper<AnnouncementStatistics> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AnnouncementStatistics::getAnnouncementId, id)
                .set(AnnouncementStatistics::getReadUserCount, (stats.getReadUserCount() == null ? 0 : stats.getReadUserCount()) + 1);
        announcementStatisticsMapper.update(null, updateWrapper);

        return R.success("ok");
    }
}
