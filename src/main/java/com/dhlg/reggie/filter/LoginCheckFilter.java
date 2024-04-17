package com.dhlg.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.dhlg.reggie.common.BaseContext;
import com.dhlg.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否登录的过滤器
 */
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter{


    //路径匹配器,由spring提供,用于支持带通配符的资源路径匹配
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();



    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response =(HttpServletResponse) servletResponse;


        log.info("拦截到请求:{}",request.getRequestURI());
        //1,获取本次请求的URI
        String requestURI = request.getRequestURI();

        //定义不需要处理的请求路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };


        //2,判断本次请求是否需要处理
        //创建check方法
        boolean check = check(urls, requestURI);


        //3,如果不需要处理,直接放行
        if(check){
            log.info("本次请求{}不需要处理",requestURI);
            filterChain.doFilter(request,response);//放行
            return;
        }

        //4-1,判断登录状态,如果已登录,则直接放行(客户端用户)
        if(request.getSession().getAttribute("employee") != null){
            log.info("用户已登录登陆,id为{}",request.getSession().getAttribute("employee"));

            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request,response);//放行
            return;
        }
        //4-2,判断登录状态,如果已登录,则直接放行(移动端用户)
        if(request.getSession().getAttribute("user") != null){
            log.info("用户已登录登陆,id为{}",request.getSession().getAttribute("user"));

            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response);//放行
            return;
        }

        //5,如果未登录则返回未登录的结果,通过输出流的方式向客户端响应数据
        log.info("用户未登录");
        //前端页面将对NOTLOGIN进行响应,从而跳转到登录页面
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        log.info("拦截到请求:{}",request.getRequestURI());
        return;


    }

    /**
     * 路径匹配,检查本次请求是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    //传入参数"不需要处理的请求路径"String[] urls,"本次请求的路径"String requestURI
    public boolean check(String[] urls,String requestURI){
        //遍历比对
        for (String url :urls){
            boolean match = PATH_MATCHER.match(url,requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}
