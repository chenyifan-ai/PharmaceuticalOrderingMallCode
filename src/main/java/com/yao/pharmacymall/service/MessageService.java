package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.entity.Message;
import com.yao.pharmacymall.mapper.MessageMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息服务类
 */
@Service
public class MessageService extends ServiceImpl<MessageMapper, Message> {

    /**
     * 获取用户消息列表
     */
    public PageResult<Message> getUserMessages(Long userId, Integer page, Integer pageSize) {
        Page<Message> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId);
        wrapper.orderByDesc(Message::getCreateTime);

        IPage<Message> result = this.page(pageInfo, wrapper);
        return PageResult.of(result.getTotal(), page, pageSize, result.getRecords());
    }

    /**
     * 标记消息为已读
     */
    public void markAsRead(Long userId, Long messageId) {
        Message message = this.getById(messageId);
        if (message != null && message.getUserId().equals(userId)) {
            message.setIsRead(1);
            message.setReadTime(LocalDateTime.now());
            this.updateById(message);
        }
    }

    /**
     * 全部标记为已读
     */
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId);
        wrapper.eq(Message::getIsRead, 0);

        List<Message> messages = this.list(wrapper);
        for (Message message : messages) {
            message.setIsRead(1);
            message.setReadTime(LocalDateTime.now());
        }
        this.updateBatchById(messages);
    }

    /**
     * 获取未读消息数量
     */
    public Long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId);
        wrapper.eq(Message::getIsRead, 0);
        return this.count(wrapper);
    }

    /**
     * 发送系统消息
     */
    public void sendSystemMessage(Long userId, Integer messageType, String title, String content, Long businessId) {
        Message message = new Message();
        message.setUserId(userId);
        message.setMessageType(messageType);
        message.setTitle(title);
        message.setContent(content);
        message.setBusinessId(businessId);
        message.setIsRead(0);

        this.save(message);
    }
}
