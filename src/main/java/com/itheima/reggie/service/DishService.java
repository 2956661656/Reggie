package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

public interface DishService extends IService<Dish> {

    //新增菜品，同时插入口味数据，需要操作两张表
    void saveWithFlavor(DishDto dishDto);

}
