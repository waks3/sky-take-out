package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("员工退出")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 员工添加
     *
     * @param employeeDTO
     * @return
     */
    @PostMapping()
    @ApiOperation("员工添加")
    public Result save(@RequestBody EmployeeDTO employeeDTO)
    {
        log.info("员工添加：{}", employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @GetMapping("page")
    @ApiOperation("员工分页查询")
    public Result<PageResult>page(EmployeePageQueryDTO employeePageQueryDTO)
    {
        log.info("员工分页查询：{}",employeePageQueryDTO);
        PageResult pageResult=employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 启用和禁用员工
     * @param status
     * @param id
     * @return
     */
    @PostMapping("status/{status}")
    @ApiOperation("启用禁止员工")
    public Result startOrStop(@PathVariable Integer status,Long id)
    {
        //如果是josn格式传输过来的话，那么就要使用@RequestBody注解来进行映射，如果是地址栏传输过来的话，那么要使用@PathVariable来进行映射，如果都不是的话，那么后端直接进行接受即可
        //这里注意了，等下的sql修改语句可以进行一个多数值的修改，这样子后续有关于员工的修改操作，都可以调用这个方法了
        employeeService.starOrStop(status,id);
        return Result.success();
    }

    /**
     * 根据员工ID查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据员工ID查询员工信息")
    public Result<Employee>getByid(@PathVariable Long id)
    {
        Employee employee=employeeService.getByid(id);
        employee.setPassword("*******");
        return Result.success(employee);
    }

    /**
     * 修改员工信息
     * @param employeeDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改员工信息")
    public Result update(@RequestBody EmployeeDTO employeeDTO)
    {
        employeeService.update(employeeDTO);
        return Result.success();
    }
}
