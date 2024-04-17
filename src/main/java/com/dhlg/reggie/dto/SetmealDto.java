package com.dhlg.reggie.dto;

import com.dhlg.reggie.entity.Setmeal;
import com.dhlg.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
