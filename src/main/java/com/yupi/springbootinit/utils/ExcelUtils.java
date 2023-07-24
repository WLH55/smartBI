package com.yupi.springbootinit.utils;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.awt.font.MultipleMaster;
import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author WLH
 * @verstion 1.0
 *
 * Excel工具类
 */
public class ExcelUtils {
    public static String excelToCsv(MultipartFile multipartFile){

        File file = null;
        try {
            file = ResourceUtils.getFile("classpath:网站数据.xlsx");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //读取数据
        List<Map<Integer, String>> list = EasyExcel.read(file)
                .excelType(ExcelTypeEnum.XLSX)
                .sheet()
                .headRowNumber(0)
                .doReadSync();
        //如果数据为空
        if(CollUtil.isEmpty(list)){
            return "";
        }
        //转换为csv格式
        StringBuilder  stringBuilder = new StringBuilder();
        //读取表头标题
        LinkedHashMap<Integer,String> headMap = (LinkedHashMap<Integer, String>) list.get(0);
        List<String> headlist = headMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
       stringBuilder.append(StringUtils.join(headlist,",")).append("\n");
        //读取数据(读完表头，从第一行开始读)
        for(int i = 1; i < list.size(); i++){
            LinkedHashMap<Integer,String> dataMap = (LinkedHashMap<Integer,String>) list.get(i);
            List<String> datalist = dataMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
            stringBuilder.append(StringUtils.join(datalist,",")).append("\n");

        }

        System.out.println(stringBuilder.toString());
        return stringBuilder.toString();

    }

    public static void main(String[] args) {
        excelToCsv(null);
    }

}
