package com.dhlg.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dhlg.reggie.entity.Category;


public interface CategoryService extends IService<Category> {

    public void remove(Long id);
}
