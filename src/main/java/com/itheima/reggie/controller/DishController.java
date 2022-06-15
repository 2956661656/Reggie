package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    public CategoryService categoryService;

    /**
     * 新增菜品
     * @param dishDto 数据传输对象
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info("数据传输对象：{}", dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页
     * @param current 当前页码
     * @param pageSize 页面展示条目数
     * @param keyWord 关键字
     */
    @GetMapping("/page")
    public R<Page> page(@RequestParam("page") Long current,
                        Long pageSize,
                        @RequestParam(value = "name", required = false) String keyWord){
        //分页构造器
        Page<Dish> pageInfo = new Page<>(current, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasLength(keyWord), Dish::getName, keyWord);
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo, queryWrapper);

        //对象拷贝，除去records
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        List<Dish> records = pageInfo.getRecords();
        List<DishDto> dtoList = records.stream().map((item) -> {    //map-取出每一个对象
            //new 一个封装类
            DishDto dishDto = new DishDto();
            //对象拷贝，包含records
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();
            //根据已有的CategoryID查询到category对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                //设置菜品名称
                dishDto.setCategoryName(category.getName());
            }
            return dishDto;
        }).collect(Collectors.toList());    //收集返回list集合

        //给records赋值
        dishDtoPage.setRecords(dtoList);

        return R.success(dishDtoPage);
    }

    /**
     * 根据ID查询菜品信息和对应的口味信息
     * @param id 前端传输的菜品ID
     * @return 返回数据传输对象（包含口味）
     */
    @GetMapping("/{id}")
    public R<DishDto> updateQuery(@PathVariable("id") Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     */
    @PutMapping
    public R<String> updateAct(@RequestBody DishDto dishDto){
        log.info("修改请求传输对象：{}", dishDto);
        dishService.updateWithFlavor(dishDto);
        return R.success("修改成功");
    }

    /**
     * 查询一类菜品下所有的菜品
     * @param dish 前端传入的菜品
     * @return 返回一个菜品列表
     */
    @GetMapping("/list")
    public R<List<Dish>> list(Dish dish){

        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus, 1);    //查询状态为在售的菜品（status=1）
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> dishes = dishService.list(queryWrapper);

        return R.success(dishes);
    }

}
