package com.yupi.springbootinit.model.dto.chart;

import lombok.Data;

/**
 * @author WLH
 * @verstion 1.0
 */
@Data
public class GenChartByAiRequest {


    /**
     * 分析目标
     */
    private String goal;
    /**
     * 图表名称
     */
    private String name;



    /**
     * 图表类型
     */
    private String chartType;



    private static final long serialVersionUID = 1L;


}
