package com.qst.lm.service;

import com.qst.lm.common.R;
import com.qst.lm.dto.PageDTO;
import com.qst.lm.dto.note.NoteDTO;
import com.qst.lm.dto.note.NoteQueryDTO;

/**
 * 笔记管理服务接口
 */
public interface INoteService {

    /**
     * 创建笔记
     *
     * @param userId 用户ID
     * @param dto    笔记信息
     * @return 操作结果
     */
    R createNote(Long userId, NoteDTO dto);

    /**
     * 更新笔记
     *
     * @param userId 用户ID
     * @param id     笔记ID
     * @param dto    笔记信息
     * @return 操作结果
     */
    R updateNote(Long userId, Long id, NoteDTO dto);

    /**
     * 删除笔记
     *
     * @param userId 用户ID
     * @param id     笔记ID
     * @return 操作结果
     */
    R deleteNote(Long userId, Long id);

    /**
     * 获取我的笔记列表
     *
     * @param userId 用户ID
     * @param query  查询条件
     * @return 笔记列表
     */
    R getNoteList(Long userId, NoteQueryDTO query);

    /**
     * 获取笔记详情
     *
     * @param userId 用户ID
     * @param id     笔记ID
     * @return 笔记详情
     */
    R getNoteDetail(Long userId, Long id);

    /**
     * 发布笔记
     *
     * @param userId 用户ID
     * @param id     笔记ID
     * @return 操作结果
     */
    R publishNote(Long userId, Long id);

    /**
     * 保存草稿
     *
     * @param userId 用户ID
     * @param dto    笔记信息
     * @return 操作结果
     */
    R saveDraft(Long userId, NoteDTO dto);

    /**
     * 获取公开笔记列表
     *
     * @param query 查询条件
     * @return 公开笔记列表
     */
    R getPublicNotes(NoteQueryDTO query);

    /**
     * 获取公开笔记详情
     *
     * @param id 笔记ID
     * @return 笔记详情
     */
    R getPublicNoteDetail(Long id);

    /**
     * 收藏笔记
     *
     * @param userId 用户ID
     * @param noteId 笔记ID
     * @return 操作结果
     */
    R collectNote(Long userId, Long noteId);

    /**
     * 取消收藏笔记
     *
     * @param userId 用户ID
     * @param noteId 笔记ID
     * @return 操作结果
     */
    R uncollectNote(Long userId, Long noteId);

    /**
     * 获取我收藏的笔记
     *
     * @param userId 用户ID
     * @param page   分页参数
     * @return 收藏笔记列表
     */
    R getCollectedNotes(Long userId, PageDTO page);

    /**
     * 获取草稿列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
     * @return 草稿列表
     */
    R getDraftList(Long userId, Integer page, Integer size);

    /**
     * 获取笔记模板
     *
     * @param type 模板类型
     * @return 笔记模板内容
     */
    R getNoteTemplate(String type);

    /**
     * 获取推荐笔记（支持匿名访问）
     * 用于产品首页展示，按热度评分排序
     * 筛选条件：isPublic=true AND status='published' AND digestStatus='digested'
     * 排序规则：hotScore = views*0.6 + likes*0.4
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 推荐笔记列表
     */
    R getRecommendedNotes(Integer pageNum, Integer pageSize);
}
