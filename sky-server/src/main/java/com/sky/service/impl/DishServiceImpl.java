package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 新增菜品和
     * @param dishDTO
     */
    //加入这个注解的是，因为要对两个数据库进行操作，所以必须要将事务合成一个，Transactional就是这个意思
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO)
    {
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //向菜品表插入一条数据
        dishMapper.insert(dish);
        //从前端可以看出，前端是无法传输给后端菜品ID的，所以我们需要在菜品插入之后，返回到菜品ID
        //注意的是，这里ID本来是无法得到的，需要在XML文件中返回
        Long dishId=dish.getId();
        //向菜品口味关系表插入N条数据
        List<DishFlavor> flavors=dishDTO.getFlavors();
        if(flavors!=null && flavors.size()>0)
        {
            //在插入口味表之前，需要遍历一遍容器，将菜品ID赋值进去
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO)
    {
        //插件分页
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO>page=dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 菜品批量删除
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids)
    {
        //判断菜品是否能够删除————是否存在起售
        for(Long id:ids)
        {
            Dish dish=dishMapper.getById(id);
            if(dish.getStatus()== StatusConstant.ENABLE)
            {
                //当前菜品处于起售中，不能删除
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //判断菜品是否能够删除————是否被套餐关联
        List<Long>setmealIds= setmealDishMapper.getSetmealIdsByDishIds(ids);
        if(setmealIds!=null && setmealIds.size()>0)
        {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除菜品表中的菜品数据
        for(Long id:ids)
        {
            dishMapper.deleteById(id);
            //删除菜品关联的口味数据
            dishFlavorMapper.deleteByDishId(id);
        }
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    public DishVO getByIdWithFlavor(Long id)
    {
        //先将菜品信息给封装起来
        Dish dish=dishMapper.getById(id);
        //其次将相应的口味信息给封装起来
        List<DishFlavor>dishFlavors =dishFlavorMapper.getByDishId(id);
        //封装到VO
        DishVO dishVO=new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    /**
     * 根据ID修改菜品基本信息和对应的口味信息
     * @param dishDTO
     */
    public void updateWithFlavor(DishDTO dishDTO)
    {
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //修改菜品表
        dishMapper.update(dish);

        //删除口味表相应的原有数据
        dishFlavorMapper.deleteByDishId(dish.getId());

        //添加口味表中相应的数据
        List<DishFlavor> flavors=dishDTO.getFlavors();
        if(flavors!=null && flavors.size()>0)
        {
            //在插入口味表之前，需要遍历一遍容器，将菜品ID赋值进去
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    public List<Dish> getBycategoryId(Long categoryId)
    {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        List<Dish>dishs=dishMapper.getBycategoryId(dish);
        return dishs;
    }

    /**
     * 用户端条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.getBycategoryId(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
    /**
     * 菜品起售停售
     *
     * @param status
     * @param id
     */
    @Transactional
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);

        if (status == StatusConstant.DISABLE) {
            // 如果是停售操作，还需要将包含当前菜品的套餐也停售
            List<Long> dishIds = new ArrayList<>();
            dishIds.add(id);
            // select setmeal_id from setmeal_dish where dish_id in (?,?,?)
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(dishIds);
            if (setmealIds != null && setmealIds.size() > 0) {
                for (Long setmealId : setmealIds) {
                    Setmeal setmeal = Setmeal.builder()
                            .id(setmealId)
                            .status(StatusConstant.DISABLE)
                            .build();
                    setmealMapper.update(setmeal);
                }
            }
        }
    }
}
