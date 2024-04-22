package com.dhlg.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhlg.reggie.common.R;
import com.dhlg.reggie.dto.DishDto;
import com.dhlg.reggie.entity.Category;
import com.dhlg.reggie.entity.Dish;
import com.dhlg.reggie.entity.DishFlavor;
import com.dhlg.reggie.service.CategoryService;
import com.dhlg.reggie.service.DishFlavorService;
import com.dhlg.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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

    private RedisTemplate redisTemplate;


    /**
     * 新增菜品
     * 接收Json数据要用@RequestBody
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){

         dishService.saveWithFlavor(dishDto);

        //清理所有缓存
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);
        //清理某个分类缓存
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);



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
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
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
        //清理所有缓存
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);
        //清理某个分类缓存
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return R.success("修改菜品成功");
    }


//    @GetMapping("/list")
//    public R<List<Dish>> listByCategoryId(Dish dish){
//        //条件构造器
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//
//        //添加筛选条件
//        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
//        queryWrapper.eq(Dish::getStatus,1);
//        //添加排序条件
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        //展示页面
//        List<Dish> list = dishService.list(queryWrapper);
//
//        return  R.success(list);
//    }
@GetMapping("/list")
public R<List<DishDto>> listByCategoryId(Dish dish) {
    List<DishDto> dishDtoList;
    //动态获取key
    String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
    //先从redis中获取缓存数据
    dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
    //存在,返回,无需查询数据库
    if(dishDtoList != null){
        return R.success(dishDtoList);
    }

    //条件构造器
    LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
    queryWrapper.eq(Dish::getStatus,1);

    queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

    List<Dish> list = dishService.list(queryWrapper);

    dishDtoList = list.stream().map((item) ->{
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(item,dishDto);
        Long categoryId = item.getCategoryId();//分类id
        //根据id查询分类对象
        Category category = categoryService.getById(categoryId);
        if(category != null){
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);
        }
        Long dishId = item.getId();

        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);

        List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);

        dishDto.setFlavors(dishFlavorList);

        return dishDto;
    }).collect(Collectors.toList());

    redisTemplate.opsForValue().set(key,dishDtoList,1, TimeUnit.HOURS);

    return R.success(dishDtoList);
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










































