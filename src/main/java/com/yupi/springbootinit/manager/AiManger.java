package com.yupi.springbootinit.manager;

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author WLH
 * @verstion 1.0
 * 对接AI平台
 */
@Service
public class AiManger {

    @Resource
    private YuCongMingClient yuCongMingClient;

    public String doChatByAi(long modelid,String message){
        //构造请求参数
        DevChatRequest devChatRequest = new DevChatRequest();
        //模型id id转成long类型
        devChatRequest.setModelId(modelid);

        devChatRequest.setMessage(message);
        //获取响应结果
        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        //如果响应为null  ，就抛出系统异常，提示“ai响应失败”
        if(response == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"ai响应失败");
        }
        return response.getData().getContent();


    }
}
