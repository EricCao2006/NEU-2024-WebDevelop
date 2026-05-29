package com.ec.onlinestore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ec.onlinestore.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}