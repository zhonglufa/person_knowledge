package com.qst.lm.service;

import com.qst.lm.common.R;

public interface IInteractionService {

    R like(Long userId, Object dto);

    R unlike(Long userId, Object dto);

    R comment(Long userId, Object dto);

    R getCommentList(Long targetId, String targetType, Long parentId, Integer page, Integer size);

    R deleteComment(Long userId, Long commentId);

    R collectContent(Long userId, Object dto);

    R uncollectContent(Long userId, Object dto);

    R getLikeCount(Long targetId, String targetType);

    R getCollectCount(Long targetId, String targetType);

    R checkLikeStatus(Long userId, Long targetId, String targetType);

    R checkCollectStatus(Long userId, Long targetId, String targetType);

    R getInteractionMessages(Long userId, String type, Integer isRead, Integer pageNum, Integer pageSize);

    R markInteractionMessageRead(Long userId, Long id);

    R markAllInteractionMessagesRead(Long userId, String type);

    R deleteInteractionMessage(Long userId, Long id);

    R getInteractionStats(Long userId);
}
