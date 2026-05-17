package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.entity.Contact;
import com.yao.pharmacymall.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 联系人管理控制器
 */
@RestController
@RequestMapping("/api/contact")
public class ContactController {

    @Autowired
    private ContactService contactService;

    /**
     * 获取当前用户的所有联系人
     */
    @GetMapping("/list")
    public Result<List<Contact>> getContactList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Contact> contacts = contactService.getListByUserId(userId);
        return Result.success(contacts);
    }

    /**
     * 获取默认联系人
     */
    @GetMapping("/default")
    public Result<Contact> getDefaultContact(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Contact contact = contactService.getDefaultContact(userId);
        return Result.success(contact);
    }

    /**
     * 添加联系人
     */
    @PostMapping("/add")
    public Result<Boolean> addContact(@RequestBody Contact contact, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        contact.setUserId(userId);
        Boolean result = contactService.save(contact);
        return Result.success(result);
    }

    /**
     * 更新联系人
     */
    @PostMapping("/update")
    public Result<Boolean> updateContact(@RequestBody Contact contact, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        contact.setUserId(userId);
        Boolean result = contactService.updateById(contact);
        return Result.success(result);
    }

    /**
     * 删除联系人
     */
    @PostMapping("/delete/{id}")
    public Result<Boolean> deleteContact(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Contact contact = contactService.getById(id);
        if (contact != null && contact.getUserId().equals(userId)) {
            Boolean result = contactService.removeById(id);
            return Result.success(result);
        }
        return Result.error("联系人不存在或无权操作");
    }

    /**
     * 设置默认联系人
     */
    @PostMapping("/setDefault/{id}")
    public Result<Boolean> setDefaultContact(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Boolean result = contactService.setDefaultContact(id, userId);
        return Result.success(result);
    }
}