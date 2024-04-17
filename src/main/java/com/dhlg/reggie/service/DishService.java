package com.dhlg.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dhlg.reggie.dto.DishDto;
import com.dhlg.reggie.entity.Dish;

import java.util.List;


public interface DishService extends IService<Dish> {

    //新增菜品同时插入菜品口味数据.操作两张表,dish,dish_flavor
    public void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品信息和口味信息
    public DishDto getByIdWithFlavor(Long id);


    //更新菜品信息,更新口味
    void updateWithFlavor(DishDto dishDto);



    public void deleteByIds(List<Long> ids);

    void setStatus0ByIds(List<Long> ids);

    void setStatus1ByIds(List<Long> ids);
}
