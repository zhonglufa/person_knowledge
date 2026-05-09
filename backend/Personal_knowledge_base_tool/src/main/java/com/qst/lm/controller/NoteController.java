package com.qst.lm.controller;

import com.qst.lm.common.R;
import com.qst.lm.dto.PageDTO;
import com.qst.lm.dto.note.NoteDTO;
import com.qst.lm.dto.note.NoteQueryDTO;
import com.qst.lm.service.INoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 笔记管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/note")
@Tag(name = "笔记管理模块", description = "笔记相关接口")
public class NoteController {

    private final INoteService noteService;

    public NoteController(INoteService noteService) {
        this.noteService = noteService;
    }

    /**
     * 创建笔记
     */
    @PostMapping
    @Operation(summary = "创建笔记", description = "创建一篇新笔记")
    public R createNote(@RequestAttribute Long userId,
                        @Valid @RequestBody NoteDTO dto) {
        return noteService.createNote(userId, dto);
    }

    /**
     * 编辑笔记
     */
    @PutMapping("/{id}")
    @Operation(summary = "编辑笔记", description = "编辑指定笔记")
    public R updateNote(@RequestAttribute Long userId,
                        @PathVariable Long id,
                        @Valid @RequestBody NoteDTO dto) {
        return noteService.updateNote(userId, id, dto);
    }

    /**
     * 删除笔记
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除笔记", description = "删除指定笔记")
    public R deleteNote(@RequestAttribute Long userId,
                        @PathVariable Long id) {
        return noteService.deleteNote(userId, id);
    }

    /**
     * 获取我的笔记列表
     */
    @GetMapping("/list")
    @Operation(summary = "我的笔记列表", description = "获取当前用户的笔记列表")
    public R getNoteList(@RequestAttribute Long userId,
                         NoteQueryDTO query) {
        return noteService.getNoteList(userId, query);
    }

    /**
     * 获取笔记详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "笔记详情", description = "获取指定笔记的详细信息")
    public R getNoteDetail(@RequestAttribute Long userId,
                           @PathVariable Long id) {
        return noteService.getNoteDetail(userId, id);
    }

    /**
     * 保存草稿
     */
    @PostMapping("/draft")
    @Operation(summary = "保存草稿", description = "保存笔记草稿")
    public R saveDraft(@RequestAttribute Long userId,
                       @Valid @RequestBody NoteDTO dto) {
        return noteService.saveDraft(userId, dto);
    }

    /**
     * 发布笔记
     */
    @PutMapping("/{id}/publish")
    @Operation(summary = "发布笔记", description = "将笔记状态改为完成并发布")
    public R publishNote(@RequestAttribute Long userId,
                         @PathVariable Long id) {
        return noteService.publishNote(userId, id);
    }

    /**
     * 获取公开笔记列表
     */
    @GetMapping("/public")
    @Operation(summary = "公开笔记列表", description = "获取所有公开笔记")
    public R getPublicNotes(NoteQueryDTO query) {
        return noteService.getPublicNotes(query);
    }

    /**
     * 获取公开笔记详情
     */
    @GetMapping("/public/{id}")
    @Operation(summary = "公开笔记详情", description = "获取指定公开笔记的详细信息")
    public R getPublicNoteDetail(@PathVariable Long id) {
        return noteService.getPublicNoteDetail(id);
    }

    /**
     * 收藏笔记
     */
    @PostMapping("/{noteId}/collect")
    @Operation(summary = "收藏笔记", description = "收藏他人的公开笔记")
    public R collectNote(@RequestAttribute Long userId,
                         @PathVariable Long noteId) {
        return noteService.collectNote(userId, noteId);
    }

    /**
     * 取消收藏笔记
     */
    @DeleteMapping("/{noteId}/collect")
    @Operation(summary = "取消收藏", description = "取消收藏笔记")
    public R uncollectNote(@RequestAttribute Long userId,
                           @PathVariable Long noteId) {
        return noteService.uncollectNote(userId, noteId);
    }

    /**
     * 获取我收藏的笔记
     */
    @GetMapping("/collected")
    @Operation(summary = "我收藏的笔记", description = "获取当前用户收藏的笔记列表")
    public R getCollectedNotes(@RequestAttribute Long userId,
                               PageDTO page) {
        return noteService.getCollectedNotes(userId, page);
    }

    // ==================== 扩展接口 ====================

    /**
     * 获取草稿列表
     */
    @GetMapping("/draft/list")
    @Operation(summary = "获取草稿列表", description = "获取当前用户的所有草稿笔记列表")
    public R getDraftList(@RequestAttribute Long userId,
                          PageDTO page) {
        log.info("用户[{}]获取草稿列表, 页码[{}], 每页[{}]", userId, page.getPageNum(), page.getPageSize());
        return noteService.getDraftList(userId, page.getPageNum(), page.getPageSize());
    }

    /**
     * 获取笔记模板
     */
    @GetMapping("/template/{type}")
    @Operation(summary = "获取笔记模板", description = "根据模板类型获取预定义的笔记模板内容")
    public R getNoteTemplate(@RequestAttribute Long userId,
                             @PathVariable String type) {
        log.info("用户[{}]获取笔记模板, 类型[{}]", userId, type);
        return noteService.getNoteTemplate(type);
    }

    /**
     * 获取推荐笔记（支持匿名访问）
     * 用于产品首页展示，按热度评分排序
     */
    @GetMapping("/recommended")
    @Operation(summary = "获取推荐笔记", description = "获取系统推荐的公开笔记列表，按热度评分排序，支持匿名访问")
    public R getRecommendedNotes(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取推荐笔记, 页码[{}], 每页[{}]", pageNum, pageSize);
        return noteService.getRecommendedNotes(pageNum, pageSize);
    }
}
