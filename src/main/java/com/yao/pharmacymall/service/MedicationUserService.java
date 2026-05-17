package com.yao.pharmacymall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.pharmacymall.config.BusinessException;
import com.yao.pharmacymall.entity.MedicationUser;
import com.yao.pharmacymall.mapper.MedicationUserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用药人服务类
 */
@Service
public class MedicationUserService extends ServiceImpl<MedicationUserMapper, MedicationUser> {

    /**
     * 获取用户用药人列表
     */
    public List<MedicationUser> getUserMedicationUsers(Long userId) {
        LambdaQueryWrapper<MedicationUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicationUser::getUserId, userId);
        wrapper.orderByDesc(MedicationUser::getIsDefault, MedicationUser::getCreateTime);
        return this.list(wrapper);
    }

    /**
     * 添加用药人
     */
    public void addMedicationUser(MedicationUser medicationUser, Long userId) {
        medicationUser.setUserId(userId);

        // 如果是默认用药人，取消其他默认
        if (medicationUser.getIsDefault() != null && medicationUser.getIsDefault() == 1) {
            setAllNonDefault(userId);
        }

        this.save(medicationUser);
    }

    /**
     * 更新用药人
     */
    public void updateMedicationUser(MedicationUser medicationUser, Long userId) {
        MedicationUser existUser = this.getById(medicationUser.getId());
        if (existUser == null || !existUser.getUserId().equals(userId)) {
            throw new BusinessException("用药人不存在");
        }

        if (medicationUser.getIsDefault() != null && medicationUser.getIsDefault() == 1) {
            setAllNonDefault(userId);
        }

        this.updateById(medicationUser);
    }

    /**
     * 删除用药人
     */
    public void deleteMedicationUser(Long id, Long userId) {
        MedicationUser medicationUser = this.getById(id);
        if (medicationUser == null || !medicationUser.getUserId().equals(userId)) {
            throw new BusinessException("用药人不存在");
        }

        this.removeById(id);
    }

    /**
     * 设置默认用药人
     */
    public void setDefaultMedicationUser(Long id, Long userId) {
        MedicationUser medicationUser = this.getById(id);
        if (medicationUser == null || !medicationUser.getUserId().equals(userId)) {
            throw new BusinessException("用药人不存在");
        }

        setAllNonDefault(userId);
        medicationUser.setIsDefault(1);
        this.updateById(medicationUser);
    }

    /**
     * 将用户所有用药人设为非默认
     */
    private void setAllNonDefault(Long userId) {
        LambdaQueryWrapper<MedicationUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicationUser::getUserId, userId);
        List<MedicationUser> users = this.list(wrapper);
        for (MedicationUser user : users) {
            user.setIsDefault(0);
        }
        this.updateBatchById(users);
    }
}
