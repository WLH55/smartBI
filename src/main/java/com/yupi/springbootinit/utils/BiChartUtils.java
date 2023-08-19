package com.yupi.springbootinit.utils;

import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public class BiChartUtils {

    /**
     * 拼接用户输入的信息
     * @param goal
     * @param chartType
     * @param multipartFile
     * @return
     */
    public static String getUserInput(String goal, String chartType, MultipartFile multipartFile) {
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");

        // 压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        return userInput.toString();
    }

    /**
     * 构建BiChart信息
     * @param chartName
     * @param goal
     * @param chartType
     * @param csvData
     * @param genChart
     * @param genResult
     * @param loginUser
     * @return
     */
    public static Chart getBiChart(String chartName, String goal, String chartType, String csvData, String genChart, String genResult, User loginUser) {
        Chart chart = new Chart();
        chart.setName(chartName);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
//        chart.setStatus("succeed");
        return chart;
    }
}