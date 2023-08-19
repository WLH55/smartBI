package com.yupi.springbootinit.constant;

import java.util.Arrays;
import java.util.List;

public interface ChartConstant {
    /**
     * AI生成的内容分隔符
     */
    String GEN_CONTENT_SPLITS = "【【【【【";

    /**
     * AI 生成的内容的元素为3个
     */
    int GEN_ITEM_NUM = 3;

    /**
     * 生成图表的数据下标
     */
    int GEN_CHART_IDX = 1;

    /**
     * 生成图表的分析结果的下标
     */
    int GEN_RESULT_IDX = 2;

    /**
     * 文件上传的大小限制1M
     */
    long File_SIZE_ONE = 1024 * 1024L;

    /**
     * 图表上传文件后缀白名单
     */
    List<String> VALID_FILE_SUFFIX= Arrays.asList("xlsx","csv","xls","json");
    /**
     * 限流前缀
     */
    String LIMIT_PREFIX= "genCharByAi_";
}