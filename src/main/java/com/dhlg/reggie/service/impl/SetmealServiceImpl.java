package com.dhlg.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dhlg.reggie.common.CustomException;
import com.dhlg.reggie.dto.SetmealDto;
import com.dhlg.reggie.entity.Dish;
import com.dhlg.reggie.entity.Setmeal;
import com.dhlg.reggie.entity.SetmealDish;
import com.dhlg.reggie.mapper.SetmealMapper;
import com.dhlg.reggie.service.SetmealDishService;
import com.dhlg.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 新增套餐,需要同时保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Transactional
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐基本信息,操作setmeal,执行insert操作
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());


        //保存套餐和菜品的关联信息,操作setmeal_dish,执行insert操作
        setmealDishService.saveBatch(setmealDishes);
    }


    @Transactional
    @Override
    public void removeWithDish(List<Long> ids) {
        //查询套餐状态,确定是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);

        //如果不能删除,抛出业务异常
        //可以删,删除套餐中的数据
        int count = this.count(queryWrapper);
        if(count > 0){
            throw new CustomException("套餐正在售卖,不能删除");
        }
        //setmeal
        this.removeByIds(ids);
        //删除关系中的数据setmeal_dish表
        //setmealDishService.removeByIds(ids);不能用是因为ids不是setmealDish的ids
        //需要先构造setmealDish的ids

        LambdaQueryWrapper<SetmealDish> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getSetmealId,ids);

        setmealDishService.remove(queryWrapper1);

    }




    @Override
    public void setStatus0ByIds(List<Long> ids) {

        LambdaQueryWrapper<Setmeal> queryWrapper  = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);

        Setmeal setmeal = new Setmeal();
        setmeal.setStatus(0);
        setmealService.update(setmeal,queryWrapper);

    }
    @Override
    public void setStatus1ByIds(List<Long> ids) {

        LambdaQueryWrapper<Setmeal> queryWrapper  = new LambdaQueryWrapper<>();

        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,0);

        Setmeal setmeal = new Setmeal();
        setmeal.setStatus(1);
        setmealService.update(setmeal,queryWrapper);

    }

    @Override
    public SetmealDto getByIdWithDish(Long id) {

        //setmeal
        //setmealDto
        Setmeal setmeal = this.getById(id);

        SetmealDto setmealDto = new SetmealDto();

        BeanUtils.copyProperties(setmeal,setmealDto);

        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();

        //查询setmeal表关联的setmealDish表
        queryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());

        List<SetmealDish> dishes = setmealDishService.list(queryWrapper);

        setmealDto.setSetmealDishes(dishes);

        return setmealDto;
    }

    /*
    @Override
    public void updateWithDish(SetmealDto setmealDto) {

        this.updateById(setmealDto);

        //



    }

     */

    @Override
    public void updateWithDish(SetmealDto setmealDto){
        // 更新套餐基本信息
        this.updateById(setmealDto);

        // 清理当前套餐菜品数据---setmeal_dish 表的 delete 操作
        // 构造条件查询
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(queryWrapper);

        // 添加当前提交过来的菜品数据---setmeal_dish 表的 insert 操作
        List<SetmealDish> dishes = setmealDto.getSetmealDishes();
        // 设置套餐 ID
        dishes.forEach(dish -> dish.setSetmealId(setmealDto.getId()));
        setmealDishService.saveBatch(dishes);
    }



}
