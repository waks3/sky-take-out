package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品ID查询对应的套餐ID
     * @param dishIds
     * @return
     */
    List<Long>getSetmealIdsByDishIds(List<Long> dishIds);

    /**
     * 批量添加套餐相应的菜品信息
     * @param list
     */
    void insertBatch(List<SetmealDish> list);

    /**
     * 根据套餐ID删除相应的菜品信息
     * @param SetmealId
     */
    @Delete("delete from setmeal_dish where setmeal_id=#{SetmealId}")
    void deleteBySetmealId(Long SetmealId);
}
