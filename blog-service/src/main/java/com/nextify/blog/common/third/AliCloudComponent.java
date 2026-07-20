package com.nextify.blog.common.third;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONString;
import cn.hutool.json.JSONUtil;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.protocol.Protocol;
import com.nextify.blog.common.ResultCode;
import com.nextify.blog.common.exception.BusinessException;
import com.nextify.blog.dto.AIChatDto;
import com.nextify.blog.vo.AIChatVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class AliCloudComponent {
    @Value("${nextify.ai-assistant.base-url}")
    private String baseUrl;

    @Value("${nextify.ai-assistant.api-key}")
    private String apiKey;

    @Value("${nextify.ai-assistant.model}")
    private String model;

    public AIChatVo callWithMessage(AIChatDto request){
        GenerationResult response;

        try {
            Generation gen = new Generation(Protocol.HTTP.getValue(), baseUrl);
            log.info(baseUrl + apiKey + model);

            Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("你是一个创作高手")
                .build();
            Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(request.getMessage())
                .build();
            GenerationParam param = GenerationParam.builder()
                // 若没有配置环境变量，请用阿里云百炼API Key将下行替换为：.apiKey("sk-xxx")
                .apiKey(apiKey)
                // 模型列表：https://help.aliyun.com/model-studio/getting-started/models
                .model(model)
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();

                response = gen.call(param);

                if(response.getStatusCode() != 200 || response == null || response.getOutput() == null){
                    log.info("Response error for AI chat");
                    return AIChatVo.builder().build();
                }
                log.info("原始响应：response = {}", response.toString());
                String content = Optional.ofNullable(response.getOutput().getChoices())
                    .filter(choices -> !choices.isEmpty())
                    .map(choices -> choices.get(0))
                    .map(GenerationOutput.Choice::getMessage)
                    .map(Message::getContent)
                    .orElse(null);

                return  AIChatVo.builder().content(content).action("聊天").build();






        }catch (ApiException e){
            log.info("e =", e);
            throw new BusinessException(ResultCode.ALIClOUD_API_EXCEPTION.getCode(), ResultCode.ALIClOUD_API_EXCEPTION.getMessage());
        }catch (NoApiKeyException e) {
            throw new BusinessException(ResultCode.ALIClOUD_NO_API.getCode(), ResultCode.ALIClOUD_NO_API.getMessage());
        } catch (InputRequiredException e){
            throw new BusinessException(ResultCode.ALIClOUD_API_INPUT_EXCEPTION.getCode(), ResultCode.ALIClOUD_API_INPUT_EXCEPTION.getMessage());
        }

    }
}
