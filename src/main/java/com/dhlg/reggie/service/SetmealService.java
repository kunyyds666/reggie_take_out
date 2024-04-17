package com.dhlg.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dhlg.reggie.dto.SetmealDto;
import com.dhlg.reggie.entity.Setmeal;

import java.util.List;


public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐,需要同时保存套餐和菜品的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐,同时删除套餐和菜品的关联信息
     * @param ids
     */
    public void removeWithDish(List<Long> ids);

    void setStatus1ByIds(List<Long> ids);

    void setStatus0ByIds(List<Long> ids);

    SetmealDto getByIdWithDish(Long id);

    void updateWithDish(SetmealDto setmealDto);
}

