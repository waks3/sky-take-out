package com.sky.service;

import com.sky.dto.DishDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


public interface DishService {
    /**
     * 新增菜品和
     * @param dishDTO
     */
    public void saveWithFlavor(DishDTO dishDTO);
}
