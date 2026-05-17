package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.dto.CHomeVO;
import com.yao.pharmacymall.dto.ProductPackageVO;
import com.yao.pharmacymall.service.CHomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/c/home")
public class CHomeController {

    @Autowired
    private CHomeService cHomeService;

    @GetMapping
    public Result<CHomeVO> getHome() {
        return Result.success(cHomeService.getHomeData());
    }

    @GetMapping("/package/{id}")
    public Result<ProductPackageVO> getPackage(@PathVariable Long id) {
        ProductPackageVO vo = cHomeService.getPackageDetail(id);
        if (vo == null) {
            return Result.error(404, "套餐不存在或已下架");
        }
        return Result.success(vo);
    }
}
