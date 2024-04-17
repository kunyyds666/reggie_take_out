package com.dhlg.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dhlg.reggie.entity.Employee;
import com.dhlg.reggie.mapper.EmployeeMapper;
import com.dhlg.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service//mybatis规范继承父类实现父接口
@Slf4j
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService{
}
