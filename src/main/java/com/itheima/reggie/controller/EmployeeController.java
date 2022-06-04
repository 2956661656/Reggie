package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.impl.EmployeeService;
import com.itheima.reggie.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param session
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpSession session, @RequestBody Employee employee){
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));

        //根据页面提交的用户名查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee aEmployee = employeeService.getOne(queryWrapper);      //因为数据库的用户名的唯一性，getOne

        //如果用户不存在则登录失败
        if (aEmployee == null){
            return R.error("用户不存在");
        }

        //用户存在再比对密码是否正确
        if (!aEmployee.getPassword().equals(password)){
            return R.error("用户名或密码错误");
        }

        //查看用户状态是否正常，是否被禁用
        if (aEmployee.getStatus() != 1){
            return R.error("用户暂时不可登录");
        }

        //登录成功
        session.setAttribute("employee",employee.getId());

        return R.success(employee);
    }

    /**
     * 登出操作
     */
    @PostMapping("/logout")
    public R<String> logout(HttpSession session){
        //清理session中保存的员工ID
        session.removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Employee employee, HttpSession session){
        log.info("新增员工：{}", employee.toString());

        //设置初始密码123456，进行md5加密
        String password = DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8));

        employee.setPassword(password);

        LocalDateTime dateTime = LocalDateTime.now();

        employee.setCreateTime(dateTime);
        log.info("员工创建时间：{}", dateTime);
        employee.setUpdateTime(dateTime);

        //获取当前登录用户的ID并设置为当前正在注册的注册人
        employee.setCreateUser((long)session.getAttribute("employee"));

        employeeService.save(employee);

        return R.success("新增员工成功");
    }

    @GetMapping("/page")
    public R<Page<Employee>> page(Integer curPage, Integer pageSize,
                        @RequestParam(required = false) String name){
        log.info("page={},pageSize={},name={}", curPage, pageSize, name);

        Page<Employee> page = new Page<>(curPage, pageSize);

        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(StringUtils.hasLength(name),Employee::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        employeeService.page(page,queryWrapper);

        return R.success(page);
    }

}
