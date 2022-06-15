package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetMealDto;
import com.itheima.reggie.entity.SetMeal;

import java.util.ArrayList;

public interface SetMealService extends IService<SetMeal> {

    /**
     * 新增套餐，同时需要保存套餐和菜皮的关联关系
     * @param setMealDto
     */
    void saveWithDish(SetMealDto setMealDto);

    /**
     * 删除套餐同时删除关联的菜品
     */
    void removeWithDish(ArrayList<Long> ids);

}
