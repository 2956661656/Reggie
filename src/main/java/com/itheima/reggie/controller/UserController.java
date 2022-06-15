package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.util.EmailUtil;
import com.itheima.reggie.util.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送短信验证码
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){

        //获取手机号码
        String phone = user.getPhone();     //这里为邮箱

        if (!StringUtils.hasLength(phone)){
            R.error("手机号码为空，短信发送失败");
        }
        //随机生成验证码
        String captcha = ValidateCodeUtils.generateValidateCode(4).toString();
        log.info("验证码：{}", captcha);

        //调用阿里云提供的短信服务API完成发送短信服务
//        SMSUtils.sendMessage("瑞吉外卖", "", phone, captcha);
        //这里调用邮箱发送
        try {
            EmailUtil.sendEmail(phone, "验证码", "您的验证码为：", captcha);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

        //将生成的验证码保存到Session，用于后续校验
        session.setAttribute(phone, captcha);

        return R.success("短信验证码发送成功");
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map<String, String> user, HttpSession session){

        //获取手机号码
        String phone = user.get("phone");
        String code = user.get("code");

        if (!StringUtils.hasLength(code)){
            R.error("验证码为空");
        }

        String captcha = (String) session.getAttribute(phone);

        if (captcha.isEmpty()){
            return R.error("请先发送验证码");
        }
        if (!code.equals(captcha)){
            return R.error("验证码错误");
        }

        //判断用户是否已经在用户表里存在，存在：直接登录；不存在，注册并登录
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone);
        User userIn = userService.getOne(queryWrapper);

        if (userIn == null){
            userIn = new User();
            userIn.setPhone(phone);
            userService.save(userIn);
        }

        session.setAttribute("user", userIn.getId());

        return R.success(userIn);
    }
}
