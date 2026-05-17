package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.PageResult;
import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.entity.Message;
import com.yao.pharmacymall.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 消息控制器
 */
@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 获取消息列表
     */
    @GetMapping("/list")
    public Result<PageResult<Message>> getMessageList(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        Long userId = (Long) request.getAttribute("userId");
        PageResult<Message> result = messageService.getUserMessages(userId, page, pageSize);
        return Result.success(result);
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/read/{id}")
    public Result<?> markAsRead(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        messageService.markAsRead(userId, id);
        return Result.success("操作成功", null);
    }

    /**
     * 全部标记为已读
     */
    @PostMapping("/readAll")
    public Result<?> markAllAsRead(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        messageService.markAllAsRead(userId);
        return Result.success("操作成功", null);
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unreadCount")
    public Result<Long> getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long count = messageService.getUnreadCount(userId);
        return Result.success(count);
    }
}
