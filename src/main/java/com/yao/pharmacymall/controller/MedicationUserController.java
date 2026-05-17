package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.entity.MedicationUser;
import com.yao.pharmacymall.service.MedicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用药人控制器
 */
@RestController
@RequestMapping("/api/medicationUser")
public class MedicationUserController {

    @Autowired
    private MedicationUserService medicationUserService;

    /**
     * 获取用药人列表
     */
    @GetMapping("/list")
    public Result<List<MedicationUser>> getList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<MedicationUser> list = medicationUserService.getUserMedicationUsers(userId);
        return Result.success(list);
    }

    /**
     * 添加用药人
     */
    @PostMapping("/add")
    public Result<?> add(HttpServletRequest request, @RequestBody MedicationUser medicationUser) {
        Long userId = (Long) request.getAttribute("userId");
        medicationUserService.addMedicationUser(medicationUser, userId);
        return Result.success("添加成功", null);
    }

    /**
     * 更新用药人
     */
    @PutMapping("/update")
    public Result<?> update(HttpServletRequest request, @RequestBody MedicationUser medicationUser) {
        Long userId = (Long) request.getAttribute("userId");
        medicationUserService.updateMedicationUser(medicationUser, userId);
        return Result.success("更新成功", null);
    }

    /**
     * 删除用药人
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        medicationUserService.deleteMedicationUser(id, userId);
        return Result.success("删除成功", null);
    }

    /**
     * 设置默认用药人
     */
    @PostMapping("/setDefault/{id}")
    public Result<?> setDefault(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        medicationUserService.setDefaultMedicationUser(id, userId);
        return Result.success("设置成功", null);
    }
}
