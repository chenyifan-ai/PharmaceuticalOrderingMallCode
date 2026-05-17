package com.yao.pharmacymall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yao.pharmacymall.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息Mapper接口
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
