package com.dhlg.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dhlg.reggie.entity.User;
import com.dhlg.reggie.mapper.UserMapper;
import com.dhlg.reggie.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService {
}
