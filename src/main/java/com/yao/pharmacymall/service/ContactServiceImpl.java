package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.pharmacymall.entity.Contact;
import com.yao.pharmacymall.mapper.ContactMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContactServiceImpl extends ServiceImpl<ContactMapper, Contact> implements ContactService {

    @Override
    public List<Contact> getListByUserId(Long userId) {
        LambdaQueryWrapper<Contact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Contact::getUserId, userId);
        return this.list(wrapper);
    }

    @Override
    @Transactional
    public Boolean setDefaultContact(Long contactId, Long userId) {
        // 先将该用户的所有联系人都设为非默认
        LambdaUpdateWrapper<Contact> resetWrapper = new LambdaUpdateWrapper<>();
        resetWrapper.eq(Contact::getUserId, userId)
                .set(Contact::getIsDefault, 0);
        this.update(resetWrapper);

        // 再将指定联系人设为默认
        Contact contact = new Contact();
        contact.setId(contactId);
        contact.setIsDefault(1);
        return this.updateById(contact);
    }

    @Override
    public Contact getDefaultContact(Long userId) {
        LambdaQueryWrapper<Contact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Contact::getUserId, userId)
                .eq(Contact::getIsDefault, 1);
        return this.getOne(wrapper);
    }
}