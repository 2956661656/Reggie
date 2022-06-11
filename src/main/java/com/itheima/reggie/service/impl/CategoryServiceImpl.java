package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.RelationException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.SetMeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetMealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;
    @Autowired
    private SetMealService setMealService;

    /**
     * 根据ID删除分类，删除之前尽心判断
     */
    @Override
    public void remove(Long id) {
        //查询当前ID分类是否关联了菜品，如果已经关联，抛出一个业务异常
        LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据分类ID查询
        dishQueryWrapper.eq(Dish::getCategoryId, id);
        int dishCount = dishService.count(dishQueryWrapper);
        //如果查询条件大于0说明当前正要删除的分类下有关联的菜品
        if (dishCount > 0){
            throw new RelationException("当前分类下关联了菜品，删除失败");
        }

        //查询当前ID分类是否关联了套餐，如果已经关联，抛出一个业务异常
        LambdaQueryWrapper<SetMeal> setMealQueryWrapper = new LambdaQueryWrapper<>();
        setMealQueryWrapper.eq(SetMeal::getCategoryId, id);
        int setMealCount = setMealService.count(setMealQueryWrapper);
        //如果查询条件大于0说明当前正要删除的分类下有关联的套餐
        if (setMealCount > 0){
            throw new RelationException("当前分类下关联了套餐，删除失败");
        }

        //无关联，正常删除
        super.removeById(id);
    }
}
