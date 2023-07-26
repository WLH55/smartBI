package com.yupi.springbootinit.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.constant.FileConstant;
import com.yupi.springbootinit.constant.UserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.manager.AiManger;
import com.yupi.springbootinit.manager.RedisLimiterManager;
import com.yupi.springbootinit.model.dto.chart.*;
import com.yupi.springbootinit.model.dto.file.UploadFileRequest;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.enums.FileUploadBizEnum;
import com.yupi.springbootinit.model.vo.BiResponse;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.service.UserService;
import com.yupi.springbootinit.utils.ExcelUtils;
import com.yupi.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 帖子接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {



    @Resource
    private UserService userService;
    @Resource
    private ChartService chartService;
    @Resource
    private AiManger aiManger;
    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addchart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);

        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newchartId = chart.getId();
        return ResultUtils.success(newchartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletechart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldchart = chartService.getById(id);
        ThrowUtils.throwIf(oldchart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldchart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatechart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldchart = chartService.getById(id);
        ThrowUtils.throwIf(oldchart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getchartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Chart>> listchartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
               getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMychartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
               getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    // endregion



    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editchart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);
        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldchart = chartService.getById(id);
        ThrowUtils.throwIf(oldchart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldchart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 图表上传，智能分析(同步方式)
     *
     * @param multipartFile
     * @param
     * @param request
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<BiResponse> genChartByAI(@RequestPart("file") MultipartFile multipartFile,
                                           GenChatByAiRequest genChatByAiRequest, HttpServletRequest request) {
        String goal = genChatByAiRequest.getGoal();
        String name = genChatByAiRequest.getName();
        String chartType = genChatByAiRequest.getChartType();
        //效验
        //如果分析目标为空，则抛出请求参数错误异常，并给出提示
        //鱼哥自己的工具类，可以自己写
        ThrowUtils.throwIf(StringUtils.isBlank(goal),ErrorCode.PARAMS_ERROR,"分析目标不能为空");
        //如果名称不为空，且长度大于100，则抛出请求参数错误异常，并给出提示
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100,ErrorCode.PARAMS_ERROR,"名称过长");
        //效验文件
        //文件白名单
        List<String> VALTD_FILE_SUFFIX = Arrays.asList("xls","xlsx");
        //限制文件大小
        long MAX_FILE_SIZE = 1 * 1024 * 1024L;
        long fileSize = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        //FileUtil  Hutool工具类
        String suffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(fileSize > MAX_FILE_SIZE,ErrorCode.PARAMS_ERROR,"文件过大");
        ThrowUtils.throwIf(!VALTD_FILE_SUFFIX.contains(suffix),ErrorCode.PARAMS_ERROR,"文件格式不正确");




        //先验证是否登录
        User loginUser = userService.getLoginUser(request);
        //用户限流,每个用户一个限流器
        redisLimiterManager.doRateLimiter("genCharByAi_"+loginUser.getId());
        //指定一个模型id（把id写死）
        long modelId = 1683130877963513857L;

//        用户的输入（参考）
//        分析需求：
//        分析网站用户的增长情况
//        原始数据：
//        日期，用户数
//        1号，10
//        2号，20
//        3号，30
//

        //用户输入
        StringBuilder userInput = new StringBuilder();
        String userGoal = goal;
        if(StringUtils.isNotBlank(chartType)){
            userGoal += ", 请使用" + chartType;
        }
        userInput.append("分析需求：").append(userGoal).append("\n");

        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append("原始数据：").append(csvData).append("\n");
        //调用AI接口
        String result = aiManger.doChatByAi(modelId,userInput.toString());
        String[] splits = result.split("【【【【");
        if(splits.length < 3){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"分析失败");
        }
        String chartCode =  splits[1].trim();
        String chartResult = splits[2].trim();
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setName(name);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setUserId(loginUser.getId());

        chart.setGenChart(chartCode);
        chart.setGenResult(chartResult);
        boolean chartSave = chartService.save(chart);
        ThrowUtils.throwIf(!chartSave,ErrorCode.SYSTEM_ERROR,"图表保存失败");

        //拿到返回结果
        //对结果进行拆分
        //对拆分结果做效验
        //设置响应体对象，返回给前端
        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(chartCode);
        biResponse.setGenResult(chartResult);
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);

    }

    /**
     * 图表上传，智能分析(异步方式)
     *
     * @param multipartFile
     * @param
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse<BiResponse> genChartByAiWithAsync(@RequestPart("file") MultipartFile multipartFile,
                                               GenChatByAiRequest genChatByAiRequest, HttpServletRequest request) {
        String goal = genChatByAiRequest.getGoal();
        String name = genChatByAiRequest.getName();
        String chartType = genChatByAiRequest.getChartType();
        //效验
        //如果分析目标为空，则抛出请求参数错误异常，并给出提示
        //鱼哥自己的工具类，可以自己写
        ThrowUtils.throwIf(StringUtils.isBlank(goal),ErrorCode.PARAMS_ERROR,"分析目标不能为空");
        //如果名称不为空，且长度大于100，则抛出请求参数错误异常，并给出提示
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100,ErrorCode.PARAMS_ERROR,"名称过长");
        //效验文件
        //文件白名单
        List<String> VALTD_FILE_SUFFIX = Arrays.asList("xls","xlsx");
        //限制文件大小
        long MAX_FILE_SIZE = 1 * 1024 * 1024L;
        long fileSize = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        //FileUtil  Hutool工具类
        String suffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(fileSize > MAX_FILE_SIZE,ErrorCode.PARAMS_ERROR,"文件过大");
        ThrowUtils.throwIf(!VALTD_FILE_SUFFIX.contains(suffix),ErrorCode.PARAMS_ERROR,"文件格式不正确");




        //先验证是否登录
        User loginUser = userService.getLoginUser(request);
        //用户限流,每个用户一个限流器
        redisLimiterManager.doRateLimiter("genCharByAi_"+loginUser.getId());
        //指定一个模型id（把id写死）
        long modelId = 1683130877963513857L;

//        用户的输入（参考）
//        分析需求：
//        分析网站用户的增长情况
//        原始数据：
//        日期，用户数
//        1号，10
//        2号，20
//        3号，30
//

        //用户输入
        StringBuilder userInput = new StringBuilder();
        String userGoal = goal;
        if(StringUtils.isNotBlank(chartType)){
            userGoal += ", 请使用" + chartType;
        }
        userInput.append("分析需求：").append(userGoal).append("\n");

        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append("原始数据：").append(csvData).append("\n");
        //先把图表保存到数据库
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setName(name);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setUserId(loginUser.getId());
        //设置任务状态为等待
        chart.setStatus("Wait");
        //插入数据库时，AI还没生成完成数据，这几个先不插入
//        chart.setGenChart();
//        chart.setGenResult();
        boolean chartSave = chartService.save(chart);
        ThrowUtils.throwIf(!chartSave,ErrorCode.PARAMS_ERROR,"图表保存失败");
        //在最终返回结果前，提交任务给AI
        //TODO 建议处理任务队列满了之后，返回给前端，提示用户稍后再试
        CompletableFuture.runAsync(() ->{
            //先修改图标状态为处理中，等执行完毕后，再修改为完成，保存执行结果;执行失败，修改为失败
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus("Running");
            boolean update = chartService.updateById(updateChart);
            if(!update){
                handleChartUpdateError(chart.getId(),"更新图标状态为Running失败");
                return;
            }

            //调用AI接口
            String result = aiManger.doChatByAi(modelId,userInput.toString());
            String[] splits = result.split("【【【【");
            if(splits.length < 3){
                handleChartUpdateError(chart.getId(),"AI生成失败");
                return;
            }
            String chartCode =  splits[1].trim();
            String chartResult = splits[2].trim();
            //调用AI得到结果后，更新图表状态
            Chart updateChart2 = new Chart();
            updateChart2.setId(chart.getId());
            updateChart2.setStatus("succeed");
            updateChart2.setGenChart(chartCode);
            updateChart2.setGenResult(chartResult);
            boolean update2 = chartService.updateById(updateChart2);
            if(!update2){
                handleChartUpdateError(chart.getId(),"更新图标状态为succeed失败");
            }

        },threadPoolExecutor);
        //拿到返回结果
        //对结果进行拆分
        //对拆分结果做效验
        //设置响应体对象，返回给前端
        BiResponse biResponse = new BiResponse();
//        biResponse.setGenChart(chartCode);
//        biResponse.setGenResult(chartResult);
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);

    }
    //上面的接口用到很多异常，直接定义一个工具
    private void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setStatus("failed");
        updateChart.setExecMessage(execMessage);
        boolean update = chartService.updateById(updateChart);
        if(!update){
            log.error("图表状态更新失败"+ chartId+","+execMessage);
        }
    }

    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }

        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();
        Long id = chartQueryRequest.getId();
        String name = chartQueryRequest.getName();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();

        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


}
