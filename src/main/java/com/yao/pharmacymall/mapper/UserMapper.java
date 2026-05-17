package com.yao.pharmacymall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yao.pharmacymall.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
