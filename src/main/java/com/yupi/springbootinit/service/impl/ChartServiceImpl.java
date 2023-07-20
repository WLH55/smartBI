package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.mapper.ChartMapper;
import com.yupi.springbootinit.service.ChartService;
import org.springframework.stereotype.Service;

/**
* @author WLH
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2023-07-20 13:34:38
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService{

}




