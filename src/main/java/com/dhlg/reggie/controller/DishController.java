package com.dhlg.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhlg.reggie.common.R;
import com.dhlg.reggie.dto.DishDto;
import com.dhlg.reggie.entity.Category;
import com.dhlg.reggie.entity.Dish;
import com.dhlg.reggie.service.CategoryService;
import com.dhlg.reggie.service.DishFlavorService;
import com.dhlg.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;


    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;


    /**
     * 新增菜品
     * 接收Json数据要用@RequestBody
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){

         dishService.saveWithFlavor(dishDto);

         return R.success("新增菜品成功");
    }


    /**
     * 分页,查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){

        //构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();


        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null,Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行分页查询
        dishService.page(pageInfo,queryWrapper);

        //对象拷贝,忽略records,需要进行处理
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        //给page对象中的categoryId一一匹配categoryName
        //Dish实体类中没有categoryName
        List<Dish> records = pageInfo.getRecords();

        List<DishDto> list = records.stream().map((item) ->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryID = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryID);
            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    /**
     * 修改菜品,根据id查询菜品信息和口味信息,进行回写
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){

        dishService.updateWithFlavor(dishDto);

        return R.success("修改菜品成功");
    }


    @GetMapping("/list")
    public R<List<Dish>> listByCategoryId(Dish dish){
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        //添加筛选条件
        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1);
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //展示页面
        List<Dish> list = dishService.list(queryWrapper);

        return  R.success(list);
    }
    /*
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(category.getType() != null,Category::getType,category.getType());
        //添加排序条件
        queryWrapper.orderByDesc(Category::getType).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }
     */

    /**
     * 批量删除菜品,或者删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        //todo
        dishService.deleteByIds(ids);


        return R.success("删除成功");
    }

    /**
     * 启售 status 0 -> 1
     * @param ids
     * @return
     */
    @PostMapping("/status/1")
    public R<String> setStatus1(@RequestParam List<Long> ids){

        dishService.setStatus1ByIds(ids);

        return R.success(("修改成功"));
    }

    /**
     * 停售 status 1 -> 0
     * @param ids
     * @return
     */
    @PostMapping("/status/0")
    public R<String> setStatus0 (@RequestParam List<Long> ids){

        dishService.setStatus0ByIds(ids);

        return R.success("修改成功");
    }














}










































