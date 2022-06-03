package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.impl.EmployeeService;
import com.itheima.reggie.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;

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

}
