package com.qst.lm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qst.lm.pojo.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 系统公告Mapper接口
 */
@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {

    /**
     * 分页查询生效中的公告列表
     * @param page 分页对象
     * @return 公告列表
     */
    IPage<Announcement> selectActiveList(Page<Announcement> page);

    /**
     * 查询管理员创建的公告列表
     * @param page 分页对象
     * @param createdBy 创建人ID
     * @return 公告列表
     */
    IPage<Announcement> selectByCreatedBy(Page<Announcement> page, @Param("createdBy") Long createdBy);
}
