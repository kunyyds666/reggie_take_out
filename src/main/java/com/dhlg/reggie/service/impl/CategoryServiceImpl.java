package com.dhlg.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


import com.dhlg.reggie.common.CustomException;
import com.dhlg.reggie.entity.Category;
import com.dhlg.reggie.entity.Dish;
import com.dhlg.reggie.entity.Setmeal;
import com.dhlg.reggie.mapper.CategoryMapper;
import com.dhlg.reggie.service.CategoryService;
import com.dhlg.reggie.service.DishService;
import com.dhlg.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;


    /**
     *根据id删除分类,删除之前进行判断,看看分类是否关联了菜品和套餐
     * @param ids
     */
    @Override
    public void remove(Long ids){
        //添加查询条件,根据分类id进行查询
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,ids);
        int count1 = dishService.count(dishLambdaQueryWrapper);
        //查询看看分类是否关联了菜品category_id
        if(count1 > 0){//说明关联了菜品,抛出异常
            throw new CustomException("当前分类下关联了菜品,无法删除");
        }
        //
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,ids);
        //查询看看分类是否关联了套餐
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        if(count2 > 0){//说明关联了套餐,抛出异常
            throw new CustomException("当前分类下关联了套餐,无法删除");

        }
        //正常删除
        super.removeById(ids);


    }
}
