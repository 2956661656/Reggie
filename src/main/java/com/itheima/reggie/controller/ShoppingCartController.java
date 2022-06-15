package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import com.itheima.reggie.util.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService cartService;

    /**
     * 添加购物车项
     *
     * @param cart 前端传入的购物车项
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart cart) {

        log.info("购物车中添加：{}", cart);

        //设置用户ID，指定当前加入购物车项所属用户
        Long userId = BaseContext.getCurrentId();
        cart.setUserId(userId);

        //查询当前正在添加的菜品或套餐是否已存在购物车，存在：number++，不需要重新添加一条数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(ShoppingCart::getUserId, userId);

        Long dishId = cart.getDishId();
        if (dishId != null) {
            //说明当前正在添加的是菜品
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            //正在添加套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, cart.getSetmealId());
        }
        //查询正在添加的项是否在购物车中存在
        ShoppingCart shoppingCart = cartService.getOne(queryWrapper);
        if (shoppingCart != null){
            //已经存在, number+1
            Integer number = shoppingCart.getNumber();
            shoppingCart.setNumber(++number);
            cartService.updateById(shoppingCart);
        } else {    //@TODO 新增时，当用户连续点击两次添加，被断点拦截，同时放行，会导致同时新增两个，下次新增直接导致service.getOne异常
            //不存在，新增
            cart.setNumber(1);
            cart.setCreateTime(LocalDateTime.now());
            cartService.save(cart);
            shoppingCart = cart;
        }

        return R.success(shoppingCart);
    }

    /**
     * 查看购物车
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){

        log.info("查看购物车");

        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> cartList = cartService.list(queryWrapper);

        return R.success(cartList);
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clean")
    public R<String> clean(){

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        cartService.remove(queryWrapper);

        return R.success("清空购物车成功 ");
    }

    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart cart){
        Long dishId = cart.getDishId();
        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);

        //判断当前传入的是菜品（dish）还是套餐（setmeal）
        if (dishId != null){
            //当前传入dish
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            //当前传入的是setmeal
            queryWrapper.eq(ShoppingCart::getSetmealId, cart.getSetmealId());
        }

        ShoppingCart shoppingCart = cartService.getOne(queryWrapper);
        //将当前购物车已存在的当前项-1
        shoppingCart.setNumber(shoppingCart.getNumber() - 1);
        cartService.updateById(shoppingCart);

        return R.success("已将当前商品-1");
    }

}
