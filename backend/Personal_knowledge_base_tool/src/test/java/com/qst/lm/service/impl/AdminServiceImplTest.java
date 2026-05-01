package com.qst.lm.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qst.lm.common.R;
import com.qst.lm.dto.announcement.AnnouncementDTO;
import com.qst.lm.exception.BusinessException;
import com.qst.lm.mapper.*;
import com.qst.lm.pojo.*;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminServiceImpl 单元测试")
class AdminServiceImplTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private CollectionMapper collectionMapper;
    @Mock
    private NoteMapper noteMapper;
    @Mock
    private AnnouncementMapper announcementMapper;
    @Mock
    private CollectionItemMapper collectionItemMapper;
    @Mock
    private NotificationMapper notificationMapper;
    @Mock
    private AdminOperationLogMapper adminOperationLogMapper;
    @Mock
    private AnnouncementStatisticsMapper announcementStatisticsMapper;

    @InjectMocks
    private AdminServiceImpl adminService;

    private Announcement testAnnouncement;

    @BeforeAll
    static void initMybatisPlusLambdaCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        assistant.setCurrentNamespace("com.qst.lm.mapper");
        TableInfoHelper.initTableInfo(assistant, Announcement.class);
        TableInfoHelper.initTableInfo(assistant, User.class);
        TableInfoHelper.initTableInfo(assistant, Collection.class);
        TableInfoHelper.initTableInfo(assistant, Note.class);
        TableInfoHelper.initTableInfo(assistant, AdminOperationLog.class);
    }

    @BeforeEach
    void setUp() {
        testAnnouncement = new Announcement();
        testAnnouncement.setId(1L);
        testAnnouncement.setTitle("测试公告");
        testAnnouncement.setContent("测试内容");
        testAnnouncement.setStatus(1);
        testAnnouncement.setType("system");
        testAnnouncement.setPriority("medium");
        testAnnouncement.setCreatedBy(100L);
        testAnnouncement.setCreatedAt(LocalDateTime.now());
    }

    // ==================== takeDownAnnouncement 测试 ====================

    @Test
    @DisplayName("下架公告 - 正常情况")
    void takeDownAnnouncement_success() {
        when(announcementMapper.selectById(1L)).thenReturn(testAnnouncement);
        when(announcementMapper.update(isNull(), any())).thenReturn(1);
        when(adminOperationLogMapper.insert(any())).thenReturn(1);

        R result = adminService.takeDownAnnouncement(100L, 1L);

        assertEquals(200, result.getCode());
        assertEquals("公告已下架", result.getMessage());
        verify(announcementMapper).update(isNull(), any());
        verify(adminOperationLogMapper).insert(any());
    }

    @Test
    @DisplayName("下架公告 - 管理员ID为空抛出异常")
    void takeDownAnnouncement_nullAdminId_throwsException() {
        assertThrows(BusinessException.class, () ->
                adminService.takeDownAnnouncement(null, 1L));
    }

    @Test
    @DisplayName("下架公告 - 公告不存在抛出异常")
    void takeDownAnnouncement_notFound_throwsException() {
        when(announcementMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () ->
                adminService.takeDownAnnouncement(100L, 999L));
    }

    @Test
    @DisplayName("下架公告 - 已下架公告重复下架抛出异常")
    void takeDownAnnouncement_alreadyTakenDown_throwsException() {
        testAnnouncement.setStatus(2);
        when(announcementMapper.selectById(1L)).thenReturn(testAnnouncement);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                adminService.takeDownAnnouncement(100L, 1L));
        assertTrue(exception.getMessage().contains("已处于下架状态"));
    }

    // ==================== deleteAnnouncement 测试 ====================

    @Test
    @DisplayName("删除公告 - 正常逻辑删除")
    void deleteAnnouncement_success() {
        when(announcementMapper.selectById(1L)).thenReturn(testAnnouncement);
        when(announcementMapper.update(isNull(), any())).thenReturn(1);
        when(adminOperationLogMapper.insert(any())).thenReturn(1);

        R result = adminService.deleteAnnouncement(100L, 1L);

        assertEquals(200, result.getCode());
        assertEquals("公告已删除", result.getMessage());
        verify(announcementMapper).update(isNull(), any());
    }

    @Test
    @DisplayName("删除公告 - 管理员ID为空抛出异常")
    void deleteAnnouncement_nullAdminId_throwsException() {
        assertThrows(BusinessException.class, () ->
                adminService.deleteAnnouncement(null, 1L));
    }

    @Test
    @DisplayName("删除公告 - 公告不存在抛出异常")
    void deleteAnnouncement_notFound_throwsException() {
        when(announcementMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () ->
                adminService.deleteAnnouncement(100L, 999L));
    }

    // ==================== getAnnouncementList 测试 ====================

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("获取公告列表 - 仅状态筛选")
    void getAnnouncementList_statusFilter() {
        Page<Announcement> mockPage = new Page<>(1, 10);
        when(announcementMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        R result = adminService.getAnnouncementList(1, 10, 1, null);

        assertEquals(200, result.getCode());
        verify(announcementMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("获取公告列表 - 仅类型筛选")
    void getAnnouncementList_typeFilter() {
        Page<Announcement> mockPage = new Page<>(1, 10);
        when(announcementMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        R result = adminService.getAnnouncementList(1, 10, null, "system");

        assertEquals(200, result.getCode());
        verify(announcementMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("获取公告列表 - 状态和类型联合筛选")
    void getAnnouncementList_statusAndTypeFilter() {
        Page<Announcement> mockPage = new Page<>(1, 10);
        when(announcementMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        R result = adminService.getAnnouncementList(1, 10, 1, "activity");

        assertEquals(200, result.getCode());
        verify(announcementMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("获取公告列表 - 无筛选条件返回全部")
    void getAnnouncementList_noFilter() {
        Page<Announcement> mockPage = new Page<>(1, 10);
        when(announcementMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        R result = adminService.getAnnouncementList(1, 10, null, null);

        assertEquals(200, result.getCode());
        verify(announcementMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取公告列表 - 分页参数边界校验")
    void getAnnouncementList_pageParamValidation() {
        when(announcementMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<>(1, 10));

        assertDoesNotThrow(() -> adminService.getAnnouncementList(null, null, null, null));
        assertDoesNotThrow(() -> adminService.getAnnouncementList(0, 0, null, null));
        assertDoesNotThrow(() -> adminService.getAnnouncementList(1, 200, null, null));
    }

    // ==================== createAnnouncement 测试 ====================

    @Test
    @DisplayName("创建公告 - 含type和priority字段")
    void createAnnouncement_withTypeAndPriority() {
        AnnouncementDTO dto = new AnnouncementDTO();
        dto.setTitle("测试公告");
        dto.setContent("测试内容");
        dto.setType("activity");
        dto.setPriority("high");

        when(announcementMapper.insert(any(Announcement.class))).thenReturn(1);
        when(adminOperationLogMapper.insert(any())).thenReturn(1);

        R result = adminService.createAnnouncement(100L, dto);

        assertEquals(200, result.getCode());
        verify(announcementMapper).insert(argThat(announcement ->
                "activity".equals(announcement.getType()) &&
                "high".equals(announcement.getPriority()) &&
                announcement.getStatus() == 0
        ));
    }

    @Test
    @DisplayName("创建公告 - type和priority为空时使用默认值")
    void createAnnouncement_defaultTypeAndPriority() {
        AnnouncementDTO dto = new AnnouncementDTO();
        dto.setTitle("测试公告");
        dto.setContent("测试内容");

        when(announcementMapper.insert(any(Announcement.class))).thenReturn(1);
        when(adminOperationLogMapper.insert(any())).thenReturn(1);

        R result = adminService.createAnnouncement(100L, dto);

        assertEquals(200, result.getCode());
        verify(announcementMapper).insert(argThat(announcement ->
                "system".equals(announcement.getType()) &&
                "medium".equals(announcement.getPriority())
        ));
    }

    // ==================== updateAnnouncement 测试 ====================

    @Test
    @DisplayName("更新公告 - 含type和priority字段")
    void updateAnnouncement_withTypeAndPriority() {
        AnnouncementDTO dto = new AnnouncementDTO();
        dto.setType("maintenance");
        dto.setPriority("urgent");

        when(announcementMapper.selectById(1L)).thenReturn(testAnnouncement);
        when(announcementMapper.update(isNull(), any())).thenReturn(1);
        when(adminOperationLogMapper.insert(any())).thenReturn(1);

        R result = adminService.updateAnnouncement(100L, 1L, dto);

        assertEquals(200, result.getCode());
        verify(announcementMapper).update(isNull(), any());
    }

    @Test
    @DisplayName("更新公告 - 公告不存在抛出异常")
    void updateAnnouncement_notFound_throwsException() {
        AnnouncementDTO dto = new AnnouncementDTO();
        when(announcementMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () ->
                adminService.updateAnnouncement(100L, 999L, dto));
    }

    // ==================== 用户状态映射测试 ====================

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("用户列表 - 状态映射为enabled/disabled字符串")
    void getUserList_statusMapping() {
        User activeUser = new User();
        activeUser.setId(1L);
        activeUser.setUsername("active");
        activeUser.setDeleted(0);

        User disabledUser = new User();
        disabledUser.setId(2L);
        disabledUser.setUsername("disabled");
        disabledUser.setDeleted(1);

        Page<User> mockPage = new Page<>(1, 10);
        mockPage.setRecords(java.util.Arrays.asList(activeUser, disabledUser));
        mockPage.setTotal(2);

        when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        R result = adminService.getUserList(new com.qst.lm.dto.PageDTO(), null, null);

        Map<String, Object> data = (Map<String, Object>) result.getData();
        java.util.List<Map<String, Object>> records =
                (java.util.List<Map<String, Object>>) data.get("records");

        assertEquals("enabled", records.get(0).get("status"));
        assertEquals("disabled", records.get(1).get("status"));
    }
}
