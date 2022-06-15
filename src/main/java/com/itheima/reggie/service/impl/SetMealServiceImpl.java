package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.RelationException;
import com.itheima.reggie.dto.SetMealDto;
import com.itheima.reggie.entity.SetMeal;
import com.itheima.reggie.entity.SetMealDish;
import com.itheima.reggie.mapper.SetMealMapper;
import com.itheima.reggie.service.SetMealDishService;
import com.itheima.reggie.service.SetMealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetMealServiceImpl extends ServiceImpl<SetMealMapper, SetMeal> implements SetMealService {

    @Autowired
    private SetMealDishService setMealDishService;

    @Override
    @Transactional
    public void saveWithDish(SetMealDto setMealDto) {
        //保存套餐的基本信息，setmeal表，insert操作
        this.save(setMealDto);

        List<SetMealDish> setmealDishes = setMealDto.getSetmealDishes();
        List<SetMealDish> dishList = setmealDishes.stream().peek(item -> {
            item.setSetmealId(setMealDto.getId());
        }).collect(Collectors.toList());

        //保存套餐和菜品的关联信息，setmeal_dish表，insert操作
        setMealDishService.saveBatch(dishList);
    }

    @Override
    @Transactional
    public void removeWithDish(ArrayList<Long> ids) {
        //查询套餐状态，启售状态可以删除
        LambdaQueryWrapper<SetMeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ids.size() != 0, SetMeal::getId, ids);
        queryWrapper.eq(SetMeal::getStatus, 1);     //1:启售

        int count = this.count(queryWrapper);

        //如果不能删除，抛出业务异常通知不可删除
        if (count > 0){
            throw new RelationException("启售状态不可删除");
        }

        //如果可以删除，先删除套餐表中的数据
        this.removeById(ids);

        //再删除菜品表中关联的菜品
        LambdaQueryWrapper<SetMealDish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(SetMealDish::getSetmealId, ids);

        setMealDishService.remove(dishLambdaQueryWrapper);

    }

}
