package com.yupi.springbootinit.utils;

import cn.hutool.core.io.FileUtil;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.model.dto.chart.GenChartByAiRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ValidationUtil {

    private static final int CHART_NAME_LEN = 100;

    public static void validateGenChartByAiRequest(GenChartByAiRequest genChartByAiRequest) {
        if (StringUtils.isBlank(genChartByAiRequest.getChartType())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分析类型不能为空");
        }

        if (StringUtils.isBlank(genChartByAiRequest.getGoal())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分析目标不能为空");
        }

        if (StringUtils.isNotBlank(genChartByAiRequest.getName())
                && genChartByAiRequest.getName().length() > CHART_NAME_LEN) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图表名称过长");
        }
    }

    public static void validateFile(MultipartFile multipartFile, long maxSize, List<String> validFileSuffixList) {
        if (multipartFile.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小超出限制");
        }
        //FileUtil  Hutool工具类
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        if (!validFileSuffixList.contains(suffix)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型不符合要求");
        }
    }
}