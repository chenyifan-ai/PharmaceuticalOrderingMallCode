package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.pharmacymall.entity.Contact;

import java.util.List;

public interface ContactService extends IService<Contact> {
    /**
     * 获取用户的所有联系人
     */
    List<Contact> getListByUserId(Long userId);

    /**
     * 设置默认联系人
     */
    Boolean setDefaultContact(Long contactId, Long userId);

    /**
     * 获取用户的默认联系人
     */
    Contact getDefaultContact(Long userId);
}