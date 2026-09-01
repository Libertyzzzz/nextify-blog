package com.nextify.blog.common.third;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.embeddings.*;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.protocol.Protocol;
import com.alibaba.dashscope.utils.Constants;
import com.nextify.blog.common.ResultCode;
import com.nextify.blog.common.exception.BusinessException;
import com.nextify.blog.common.properties.AIAssistantProperty;
import com.nextify.blog.dto.AIChatDto;
import com.nextify.blog.vo.AIChatVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class AliCloudComponent {

    @Resource
    private AIAssistantProperty aiAssistantProperty;

    static {
        Constants.baseHttpApiUrl= "https://llm-wgwccf801w53j5be.cn-beijing.maas.aliyuncs.com/api/v1";
    }


    public AIChatVo callWithMessage(AIChatDto request){
        GenerationResult response;

        try {
            Generation gen = new Generation(Protocol.HTTP.getValue(), aiAssistantProperty.getBaseUrl());

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
                .apiKey(aiAssistantProperty.getApiKey())
                // 模型列表：https://help.aliyun.com/model-studio/getting-started/models
                .model(aiAssistantProperty.getModel())
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();

                response = gen.call(param);

                if(response.getStatusCode() != 200 || response == null || response.getOutput() == null){
                    log.info("Response error for AI chat");
                    return AIChatVo.builder().build();
                }
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

    /**
     * 通用方法，用于接收 Dashscope 消息列表并调用通义千问
     * @param dashscopeMessages 格式化后的 Dashscope 消息列表
     * @param modelToUse 使用的模型名称
     * @return GenerationResult 原始响应结果
     */
    public GenerationResult callWithDashscopeMessages(List<Message> dashscopeMessages, String modelToUse) {
        try {
            Generation gen = new Generation(Protocol.HTTP.getValue(), aiAssistantProperty.getBaseUrl());
            GenerationParam param = GenerationParam.builder()
                .apiKey(aiAssistantProperty.getApiKey())
                .model(modelToUse)
                .messages(dashscopeMessages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();

            GenerationResult response = gen.call(param);

            if (response.getStatusCode() != 200) {
                log.error("AliCloud API returned non-200 status: {}", response.getStatusCode());
                throw new BusinessException(ResultCode.ALIClOUD_API_EXCEPTION.getCode(), "AliCloud API error: " + response.getStatusCode());
            }
            return response;

        } catch (ApiException e) {
            log.error("AliCloud API Exception: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.ALIClOUD_API_EXCEPTION.getCode(), ResultCode.ALIClOUD_API_EXCEPTION.getMessage() + ": " + e.getMessage());
        } catch (NoApiKeyException e) {
            log.error("AliCloud No API Key Exception: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.ALIClOUD_NO_API.getCode(), ResultCode.ALIClOUD_NO_API.getMessage());
        } catch (InputRequiredException e) {
            log.error("AliCloud Input Required Exception: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.ALIClOUD_API_INPUT_EXCEPTION.getCode(), ResultCode.ALIClOUD_API_INPUT_EXCEPTION.getMessage() + ": " + e.getMessage());
        }
    }

    public List<TextEmbeddingResultItem> textToEmbedding(String text){

        try {
            // TextEmbedding gen = new TextEmbedding(aiAssistantProperty.getBaseUrl());

            // 构建请求参数
            TextEmbeddingParam param = TextEmbeddingParam
                .builder()
                //.apiKey(aiAssistantProperty.getApiKey())
                .model(aiAssistantProperty.getEmbeddingModel())
                // 输入文本
                .texts(Collections.singleton(text))
                .build();

            TextEmbedding textEmbedding = new TextEmbedding();
            TextEmbeddingResult textEmbeddingResult = textEmbedding.call(param);

            return  Optional.ofNullable(textEmbeddingResult)
                .map(TextEmbeddingResult::getOutput)
                .map(TextEmbeddingOutput::getEmbeddings)
                .orElse(Collections.emptyList());


        } catch (NoApiKeyException e) {
            log.error("AliCloud No API Key Exception: {}", e.getMessage());
            throw new BusinessException(ResultCode.ALIClOUD_NO_API.getCode(), ResultCode.ALIClOUD_NO_API.getMessage());
        }catch (ApiException e){
            log.error("Api exception: {}", e.getMessage());
            throw new BusinessException(ResultCode.ALIClOUD_API_EXCEPTION.getCode(), ResultCode.ALIClOUD_API_EXCEPTION.getMessage());

        }

    }

    public String   embeddingBatch(List<String> texts){
        return "";
    }


}