package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetMealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.SetMeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetMealDishService;
import com.itheima.reggie.service.SetMealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */
@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetMealController {

    @Autowired
    private SetMealService setmealService;
    @Autowired
    private SetMealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     * @param setMealDto 数据传输对象
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetMealDto setMealDto){

        log.info("当前正在保存{}套餐", setMealDto.toString());
        setmealService.saveWithDish(setMealDto);

        return R.success("新增套餐成功");
    }

    /**
     * 分页查询
     * @param current 当前页码
     * @param pageSize 每页展示数量
     * @param name 搜索框中的关键字
     */
    @GetMapping("/page")
    public R<Page<SetMealDto>> page(@RequestParam(value = "page", defaultValue = "1") Integer current,
                                 Integer pageSize,
                                 String name){
        //分页构造器
        Page<SetMeal> pageInfo = new Page<>(current, pageSize);
        Page<SetMealDto> dtoPageInfo = new Page<>();

        //条件构造器
        LambdaQueryWrapper<SetMeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasLength(name), SetMeal::getName, name);
        //根据更新时间，降序排列
        queryWrapper.orderByDesc(SetMeal::getUpdateTime);

        setmealService.page(pageInfo, queryWrapper);

        /*
        由于setmeal未展示分类名称，所以用setmealDto作传输对象
         */
        BeanUtils.copyProperties(pageInfo, dtoPageInfo, "records");
        List<SetMeal> records = pageInfo.getRecords();
        List<SetMealDto> setMeals = records.stream().map(item -> {
            SetMealDto setMealDto = new SetMealDto();

            BeanUtils.copyProperties(item, setMealDto);

            Long categoryId = item.getCategoryId();
            //根据分类ID查对象
            Category category = categoryService.getById(categoryId);
            setMealDto.setCategoryName(category.getName());
            return setMealDto;
        }).collect(Collectors.toList());

        dtoPageInfo.setRecords(setMeals);

        return R.success(dtoPageInfo);
    }

    /**
     * 批量删除
     * @param ids 前端传入的多个套餐ID
     */
    @DeleteMapping
    public R<String> delete(@RequestParam ArrayList<Long> ids){   //@TODO 记得看ArrayList是怎么被解析的
        log.info("批量删除ID{}", ids);
        setmealService.removeWithDish(ids);
        return R.success("套餐数据删除成功");
    }
}
