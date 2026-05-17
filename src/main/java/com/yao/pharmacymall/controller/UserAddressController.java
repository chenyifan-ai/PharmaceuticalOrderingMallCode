package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.ChinaRegionData;
import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.dto.AddressParseResult;
import com.yao.pharmacymall.entity.UserAddress;
import com.yao.pharmacymall.service.UserAddressService;
import com.yao.pharmacymall.util.AddressParseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 用户地址控制器
 */
@RestController
@RequestMapping("/api/address")
public class UserAddressController {

    @Autowired
    private UserAddressService userAddressService;

    /**
     * 获取地址列表
     */
    @GetMapping("/list")
    public Result<List<UserAddress>> getAddressList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<UserAddress> list = userAddressService.getUserAddresses(userId);
        return Result.success(list);
    }

    /**
     * 获取默认收货地址
     */
    @GetMapping("/default")
    public Result<UserAddress> getDefaultAddress(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserAddress address = userAddressService.getDefaultAddress(userId);
        return Result.success(address);
    }

    /**
     * 省市区级联数据
     */
    @GetMapping("/regions")
    public Result<List<Map<String, Object>>> getRegions() {
        return Result.success(ChinaRegionData.tree());
    }

    /**
     * 添加地址
     */
    @PostMapping("/add")
    public Result<?> addAddress(HttpServletRequest request, @RequestBody UserAddress address) {
        Long userId = (Long) request.getAttribute("userId");
        userAddressService.addAddress(address, userId);
        return Result.success("添加成功", null);
    }

    /**
     * 更新地址
     */
    @PutMapping("/update")
    public Result<?> updateAddress(HttpServletRequest request, @RequestBody UserAddress address) {
        Long userId = (Long) request.getAttribute("userId");
        userAddressService.updateAddress(address, userId);
        return Result.success("更新成功", null);
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteAddress(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        userAddressService.deleteAddress(id, userId);
        return Result.success("删除成功", null);
    }

    /**
     * 设置默认地址
     */
    @PostMapping("/setDefault/{id}")
    public Result<?> setDefaultAddress(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        userAddressService.setDefaultAddress(id, userId);
        return Result.success("设置成功", null);
    }

    /**
     * 智能解析粘贴的收货信息
     */
    @PostMapping("/parse")
    public Result<AddressParseResult> parseAddress(@RequestBody Map<String, String> body) {
        Map<String, String> parsed = AddressParseUtil.parse(body.get("text"));
        AddressParseResult result = new AddressParseResult();
        result.setName(parsed.get("name"));
        result.setPhone(parsed.get("phone"));
        result.setProvince(parsed.get("province"));
        result.setCity(parsed.get("city"));
        result.setDistrict(parsed.get("district"));
        result.setDetail(parsed.get("detail"));
        return Result.success(result);
    }
}
