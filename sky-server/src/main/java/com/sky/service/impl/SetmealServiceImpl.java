package com.sky.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    SetmealMapper setmealMapper;
    @Autowired
    SetmealDishMapper setmealDishMapper;
    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO)
    {
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        //新增套餐表数据
        setmealMapper.insert(setmeal);
        Long setmealId=setmeal.getId();
        //新增套餐菜品关系表数据
        List<SetmealDish> list=setmealDTO.getSetmealDishes();
        if(list!=null && list.size()>0)
        {
            //在插入口味表之前，需要遍历一遍容器，将菜品ID赋值进去
            list.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);
            });
            setmealDishMapper.insertBatch(list);
        }
    }


    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO)
    {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        //动态查询
        Page<SetmealVO>page=setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }


    /**
     * 套餐批量删除
     * @param ids
     */
    public void deleteBatch(List<Long> ids)
    {
        //判断当前套餐是否为起售状态
        for(Long id:ids)
        {
            Setmeal setmeal=setmealMapper.getById(id);
            if(setmeal.getStatus()== StatusConstant.ENABLE)
            {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

        //批量删除套餐表中的数据
        for(Long id:ids)
        {
            setmealMapper.deleteById(id);
            //删除相应的套餐菜品关系表
            setmealDishMapper.deleteBySetmealId(id);
        }
    }


    /**
     * 根据ID查询套餐
     * @param id
     * @return
     */
    public SetmealVO getByIdWithDish(Long id)
    {
        Setmeal setmeal=setmealMapper.getById(id);
        List<SetmealDish>list=setmealDishMapper.getBySetmealId(id);


        SetmealVO setmealVO=new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(list);
        return setmealVO;
    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    public void updateWithDish(SetmealDTO setmealDTO)
    {
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        //先将套餐表中的数据进行修改
        setmealMapper.update(setmeal);

        //删除原先套餐菜品表中相应的数据
        setmealDishMapper.deleteBySetmealId(setmeal.getId());

        List<SetmealDish>list=setmealDTO.getSetmealDishes();
        if (list!=null && list.size()>0)
        {
            list.forEach(setmealDish->{
                setmealDish.setSetmealId(setmeal.getId());
            });
            //重新插入前端传输过来的数据
            setmealDishMapper.insertBatch(list);
        }
    }
    /**
     * 套餐起售、停售
     * @param status
     * @param id
     */
    public void startOrStop(Long status, Long id)
    {
        setmealMapper.updateStatusById(status,id);
    }
}
