package com.dhlg.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dhlg.reggie.common.CustomException;
import com.dhlg.reggie.dto.DishDto;
import com.dhlg.reggie.entity.Dish;
import com.dhlg.reggie.entity.DishFlavor;
import com.dhlg.reggie.mapper.DishMapper;
import com.dhlg.reggie.service.DishFlavorService;
import com.dhlg.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private DishService dishService;


    @Transactional//
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        Long dishId = dishDto.getId();//菜品Id

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        //通过数据流的方式保存数据
        flavors.stream().map((item)->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        //保存菜品口味的基本信息到菜品表dish_flavor
        dishFlavorService.saveBatch(flavors);


    }

    /**
     * 根据id查询菜品信息和口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息,从dish表查询
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();

        BeanUtils.copyProperties(dish,dishDto);


        //查询当前菜品对应的口味信息,从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(DishFlavor::getDishId,dish.getId());

        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);

        dishDto.setFlavors(flavors);

        return dishDto;
    }


    @Override
    public void updateWithFlavor(DishDto dishDto){
        //更新dish表基本信息
        this.updateById(dishDto);

        //清理当前菜品口味数据---dish_flavor表的delete操作
        //条件构造器
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        //
        dishFlavorService.remove(queryWrapper);

        //添加当前提交过来的口味数据---dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();
        //通过数据流的方式保存数据
        flavors.stream().map((item)->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        

        dishFlavorService.saveBatch(flavors);


    }

    @Transactional
    public void deleteByIds(List<Long> ids) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //查询ids中的status
        queryWrapper.in(Dish::getId,ids);
        queryWrapper.eq(Dish::getStatus,1);
        //对status进行统计
        int count = this.count(queryWrapper);
        if(count > 0){
            throw new CustomException("选择的菜品中有正在售卖的,不许删除");
        }

        this.removeByIds(ids);


    }

    @Override
    public void setStatus0ByIds(List<Long> ids) {

        LambdaQueryWrapper<Dish> queryWrapper  = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId,ids);
        queryWrapper.eq(Dish::getStatus,1);

        Dish dish = new Dish();
        dish.setStatus(0);
        dishService.update(dish,queryWrapper);

    }

    @Override
    public void setStatus1ByIds(List<Long> ids) {
        LambdaQueryWrapper<Dish> queryWrapper  = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId,ids);
        queryWrapper.eq(Dish::getStatus,0);

        Dish dish = new Dish();
        dish.setStatus(1);
        dishService.update(dish,queryWrapper);
    }


}











































