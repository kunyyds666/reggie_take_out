package com.dhlg.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhlg.reggie.common.R;
import com.dhlg.reggie.entity.Employee;
import com.dhlg.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request,@RequestBody Employee employee){


        //1,将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        /*
          3.18
         */
        //2,根据页面信息提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //创建LambdaQueryWrapper对象需要指定实体类即是Employee
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        //查询方法前端传来的 employee::getUsername 与数据库中的employee 通过eq方法等于查询
        /*
          eq()方法:等于查询
          ne()方法:不相等查询
          gt()方法:大于查询
          ge()方法:大于等于查询;
          lt()方法:小于查询
          le()方法:小于等于查询
          like()方法:模糊查询
          in()方法:IN查询
         */
        Employee emp = employeeService.getOne(queryWrapper);
        //用户名有unique约束 用getOne()方法查询唯一

        //3,如果没有查询到则返回登录失败结果
        if(emp == null){
            return R.error("登录失败");
        }

        //4,密码比对,如果不一致返回登录失败结果
        if(!emp.getPassword().equals(password)){
            return R.error("登陆失败密码错误");
        }

        //5,查看员工状态,如果为已禁用状态,则返回员工已禁用结果
        if(emp.getStatus() == 0){
            return R.error("账号已禁用");
        }

        //6,登录成功,将员工id存入session并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        //清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工,员工信息{}",employee.toString());
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));//设置默认密码,md5加密

        // employee.setCreateTime(LocalDateTime.now());
        // employee.setUpdateTime(LocalDateTime.now());

        // //获得当前用户的id
        // Long empId = (Long) request.getSession().getAttribute("employee");

        // employee.setCreateUser(empId);
        // employee.setUpdateUser(empId);

        employeeService.save(employee);

        return R.success("新增员工成功");
    }

    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("page = {},pageSize = {},name = {}",page,pageSize,name);

        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        //过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 根据id修改员工信息
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest servletRequest,@RequestBody Employee employee){

        //Long empID = (Long) servletRequest.getSession().getAttribute("employee");
        //employee.setUpdateTime(LocalDateTime.now());
        //employee.setUpdateUser(empID);
        employeeService.updateById(employee);

        return R.success("员工信息修改成功 ");
    }
//
//

//   @DeleteMapping("/{id}")
//   public R<Employee> delete(@PathVariable Long id){
//       employeeService.removeById(id);
//       Employee employee = employeeService.getById(id);
//       return R.success(employee);


//   }
    @DeleteMapping
    public R<String> delete(Long id){

        employeeService.removeById(id);
        return R.success("删除成功");
    }


    /**
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        Employee employee = employeeService.getById(id);
        return R.success(employee);
    }











}



































