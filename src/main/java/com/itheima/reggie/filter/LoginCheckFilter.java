package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.R;
import com.itheima.reggie.util.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 检查用户是否登录
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter",
        urlPatterns = "/*")
public class LoginCheckFilter implements Filter {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        //获取本次请求的URI
        String uri = request.getRequestURI();
        log.info("登录过滤器正在检查: {}", uri);

        //不需要处理(拦截)的请求
        String[] notHandleURI = {
                "/employee/login",
                "employee/logout",
                "/backend/**",
                "/front/**"
        };

        //判断本次请求是否需要处理
        boolean needHandle = check(notHandleURI, uri);

        //如果有匹配的，说明需要处理，不过滤，直接放行
        if (needHandle) {
            log.info("登录过滤器检测到需要放行的请求：访问固定页面、静态资源");
            filterChain.doFilter(request, response);
            return;
        }

        //判断是否已经登录
        HttpSession session = request.getSession();
        Object employee = session.getAttribute("employee");
        //已登录，放行
        if (employee != null) {
            log.info("登录过滤器检测到需要放行的请求：{}号用户已登录", employee);

            long threadId = Thread.currentThread().getId();
            log.info("线程ID为{}", threadId);

            Long empId = (Long) employee;
            BaseContext.setCurrentId(empId);     //将当前登录的用户设置到当前线程，用于共享

            filterChain.doFilter(request, response);
            return;
        }

        //未登录
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    private boolean check(String[] uris, String requestUri) {

        for (String uri : uris) {
            boolean result = pathMatcher.match(uri, requestUri);
            if (result) {
                return true;
            }
        }
        return false;
    }

}
